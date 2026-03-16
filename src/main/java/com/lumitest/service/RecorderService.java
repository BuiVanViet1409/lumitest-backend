package com.lumitest.service;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.lumitest.model.RecordingSession;
import com.lumitest.model.RecorderEvent;
import com.lumitest.model.TestStep;
import com.lumitest.model.TestCase;
import com.lumitest.config.LumiTestConfig;
import com.lumitest.repository.TestCaseRepository;
import com.lumitest.repository.TestStepRepository;
import com.lumitest.util.HeuristicUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecorderService {

    private final StepBuilderService stepBuilderService;

    private final TestCaseRepository testCaseRepo;

    private final TestStepRepository testStepRepo;

    private final ElementLibraryService elementLibraryService;

    private final LumiTestConfig config;

    private final Map<String, RecordingSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Page> sessionPages = new ConcurrentHashMap<>();

    private Playwright playwright;
    private BrowserContext persistentContext;

    private synchronized BrowserContext getOrCreateContext() {
        if (playwright == null) {
            playwright = Playwright.create();
        }
        if (persistentContext == null) {
            try {
                BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                        .setHeadless(config.getRecorder().isHeadless())
                        .setChannel(config.getRecorder().getBrowser())
                        .setSlowMo(config.getRecorder().getDelayMs())
                        .setArgs(Arrays.asList("--no-sandbox", "--disable-setuid-sandbox"));

                Path path = Paths.get(config.getRecorder().getSessionPath())
                        .toAbsolutePath();
                log.info("🚀 Launching {} with persistent context at: {}", config.getRecorder().getBrowser(), path);

                persistentContext = playwright.chromium().launchPersistentContext(path, options);
            } catch (Exception e) {
                log.error("❌ Failed to launch browser: {}", e.getMessage());
                // Try small cleanup if it looks like a lock issue
                try {
                    Path lockFile = Paths.get(config.getRecorder().getSessionPath(),
                            "SingletonLock");
                    if (Files.exists(lockFile)) {
                        log.warn("Found SingletonLock, attempting to remove...");
                        Files.delete(lockFile);
                    }
                } catch (Exception ignored) {
                }
                throw e;
            }
        }
        return persistentContext;
    }

    public RecordingSession start(String targetUrl, String name, String description, String preconditions) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        RecordingSession session = new RecordingSession();
        session.setId(sessionId);
        session.setTargetUrl(targetUrl);
        session.setName(name != null ? name : "Recorded Test " + sessionId);
        session.setDescription(description);
        session.setPreconditions(preconditions);
        session.setActive(true);
        sessions.put(sessionId, session);

        // Run Playwright interaction in a background thread
        new Thread(() -> {
            try {
                BrowserContext context;
                try {
                    context = getOrCreateContext();
                } catch (Exception e) {
                    log.error("Could not obtain browser context", e);
                    session.setActive(false);
                    return;
                }

                Page page;
                try {
                    page = context.newPage();
                } catch (com.microsoft.playwright.PlaywrightException e) {
                    log.warn("Existing context is broken, attempting recovery...");
                    // Reset context and retry once
                    resetSession();
                    try {
                        context = getOrCreateContext();
                        page = context.newPage();
                    } catch (Exception e2) {
                        log.error("Full recovery failed", e2);
                        session.setActive(false);
                        return;
                    }
                }
                sessionPages.put(sessionId, page);

                // Expose function to receive events
                page.exposeBinding("recorderPostEvent", (source, args) -> {
                    Map<String, Object> data = (Map<String, Object>) args[0];
                    RecorderEvent event = convertToEvent(data);
                    addEvent(sessionId, event);
                    return null;
                });

                // Inject sophisticated recording script
                try {
                    byte[] scriptBytes = new ClassPathResource("recorder.js")
                            .getInputStream().readAllBytes();
                    page.addInitScript(new String(scriptBytes, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    log.error("Failed to load recorder.js", e);
                }

                page.navigate(targetUrl);

                // Monitoring session status
                while (session.isActive()) {
                    if (page.isClosed()) {
                        session.setActive(false);
                        break;
                    }
                    Thread.sleep(1000);
                }

                if (!page.isClosed()) {
                    page.close();
                }
                sessionPages.remove(sessionId);

            } catch (Exception e) {
                log.error("Error in recording session " + sessionId, e);
                session.setActive(false);
            }
        }).start();

        return session;
    }

    public synchronized void addEvent(String sessionId, RecorderEvent event) {
        RecordingSession session = sessions.get(sessionId);
        if (session != null && session.isActive()) {
            TestStep step = stepBuilderService.build(event, session.getSteps().size() + 1);
            session.getSteps().add(step);

            // Tự động học: Nếu selector là high-priority (ID/TestID), lưu vào Element
            // Library
            // In business mode, we skip selector-based element library for now
            // or we could use the label if extracted.

            log.info("Recorded step: {}", step.getDescription());
        }
    }

    public RecordingSession stop(String sessionId) {
        RecordingSession session = sessions.get(sessionId);
        if (session != null) {
            session.setActive(false);

            // Interaction logic in the thread will close the page

            // Save to DB
            TestCase testCase = new TestCase();
            testCase.setName(session.getName());
            testCase.setDescription(session.getDescription() != null ? session.getDescription()
                    : "Recorded from " + session.getTargetUrl());
            testCase.setPreconditions(session.getPreconditions());

            testCase = testCaseRepo.save(testCase);

            for (TestStep step : session.getSteps()) {
                step.setTestCaseId(testCase.getId());
                testStepRepo.save(step);
            }
        }
        return session;
    }

    public synchronized void resetSession() {
        try {
            if (persistentContext != null) {
                persistentContext.close();
                persistentContext = null;
            }
            if (playwright != null) {
                playwright.close();
                playwright = null;
            }
            // Clear the directory manually if needed
            log.info("Browser session reset successfully.");
        } catch (Exception e) {
            log.error("Failed to reset browser session", e);
        }
    }

    public RecordingSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    private RecorderEvent convertToEvent(Map<String, Object> data) {
        RecorderEvent event = new RecorderEvent();
        event.setAction((String) data.get("action"));
        event.setSelector((String) data.get("selector"));
        event.setTagName((String) data.get("tagName"));
        event.setText((String) data.get("text"));
        event.setValue((String) data.get("value"));
        event.setAttributes((Map<String, String>) data.get("attributes"));
        return event;
    }
}
