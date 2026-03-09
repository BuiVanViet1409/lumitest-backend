package com.lumitest.service;

import com.microsoft.playwright.*;
import com.lumitest.model.*;
import com.lumitest.repository.TestCaseRepository;
import com.lumitest.repository.TestStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RecorderService {

    @Autowired
    private StepBuilderService stepBuilderService;

    @Autowired
    private TestCaseRepository testCaseRepo;

    @Autowired
    private TestStepRepository testStepRepo;

    @Autowired
    private ElementLibraryService elementLibraryService;

    private final Map<String, RecordingSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Browser> browsers = new ConcurrentHashMap<>();

    public RecordingSession start(String targetUrl) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        RecordingSession session = new RecordingSession();
        session.setId(sessionId);
        session.setTargetUrl(targetUrl);
        session.setActive(true);
        sessions.put(sessionId, session);

        // Run Playwright in a background thread or manage carefully
        new Thread(() -> {
            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
                browsers.put(sessionId, browser);

                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                // Expose function to receive events
                page.exposeBinding("recorderPostEvent", (source, args) -> {
                    Map<String, Object> data = (Map<String, Object>) args[0];
                    RecorderEvent event = convertToEvent(data);
                    addEvent(sessionId, event);
                    return null;
                });

                // Inject sophisticated recording script covering all components
                try {
                    byte[] scriptBytes = new org.springframework.core.io.ClassPathResource("recorder.js")
                            .getInputStream().readAllBytes();
                    page.addInitScript(new String(scriptBytes, java.nio.charset.StandardCharsets.UTF_8));
                } catch (Exception e) {
                    log.error("Failed to load recorder.js", e);
                }

                page.navigate(targetUrl);

                // Keep browser open until session inactive
                while (session.isActive()) {
                    Thread.sleep(1000);
                }
                browser.close();
            } catch (Exception e) {
                e.printStackTrace();
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
            if (com.lumitest.util.HeuristicUtils.isHighPrioritySelector(step.getSelector())) {
                elementLibraryService.saveElement(session.getTargetUrl(), event.getText(), step.getSelector());
                log.info("🧠 LumiTest learned: Label [{}] -> Selector [{}]", event.getText(), step.getSelector());
            }

            log.info("Recorded step: {} on {}", step.getAction(), step.getSelector());
        }
    }

    public RecordingSession stop(String sessionId, String testCaseName) {
        RecordingSession session = sessions.get(sessionId);
        if (session != null) {
            session.setActive(false);

            // Save to DB
            TestCase testCase = new TestCase();
            testCase.setName(testCaseName);
            testCase.setDescription("Recorded from " + session.getTargetUrl());
            testCase = testCaseRepo.save(testCase);

            for (TestStep step : session.getSteps()) {
                step.setTestCaseId(testCase.getId());
                testStepRepo.save(step);
            }
        }
        return session;
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
