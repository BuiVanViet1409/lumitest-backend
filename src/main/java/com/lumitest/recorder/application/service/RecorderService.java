package com.lumitest.recorder.application.service;

import com.lumitest.recorder.domain.model.RecorderEvent;
import com.lumitest.recorder.domain.model.RecordingSession;
import com.lumitest.recorder.domain.port.in.ManageRecordingUseCase;
import com.lumitest.testmanagement.domain.model.TestCase;
import com.lumitest.testmanagement.domain.model.TestStep;
import com.lumitest.testmanagement.domain.port.out.TestCaseRepositoryPort;
import com.lumitest.testmanagement.domain.port.out.TestStepRepositoryPort;
import com.lumitest.testmanagement.application.service.StepBuilderService;
import com.lumitest.config.LumiTestConfig;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecorderService implements ManageRecordingUseCase {

    private final StepBuilderService stepBuilderService;
    private final TestCaseRepositoryPort testCaseRepositoryPort;
    private final TestStepRepositoryPort testStepRepositoryPort;
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
            BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                    .setHeadless(config.getRecorder().isHeadless())
                    .setChannel(config.getRecorder().getBrowser())
                    .setSlowMo(config.getRecorder().getDelayMs());

            Path path = Paths.get(config.getRecorder().getSessionPath()).toAbsolutePath();
            persistentContext = playwright.chromium().launchPersistentContext(path, options);
        }
        return persistentContext;
    }

    @Override
    public RecordingSession startRecording(String targetUrl, String name, String description, String preconditions) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        RecordingSession session = RecordingSession.builder()
                .id(sessionId)
                .targetUrl(targetUrl)
                .name(name != null ? name : "Recorded Test " + sessionId)
                .description(description)
                .preconditions(preconditions)
                .steps(new ArrayList<>())
                .active(true)
                .build();
        sessions.put(sessionId, session);

        new Thread(() -> {
            try {
                BrowserContext context = getOrCreateContext();
                Page page = context.newPage();
                sessionPages.put(sessionId, page);

                page.exposeBinding("recorderPostEvent", (source, args) -> {
                    Map<String, Object> data = (Map<String, Object>) args[0];
                    addEvent(sessionId, convertToEvent(data));
                    return null;
                });

                byte[] scriptBytes = new ClassPathResource("recorder.js").getInputStream().readAllBytes();
                page.addInitScript(new String(scriptBytes, StandardCharsets.UTF_8));

                page.navigate(targetUrl);

                while (session.isActive() && !page.isClosed()) {
                    Thread.sleep(1000);
                }
                
                if (!page.isClosed()) page.close();
                sessionPages.remove(sessionId);
            } catch (Exception e) {
                log.error("Error in recording session " + sessionId, e);
                session.setActive(false);
            }
        }).start();

        return session;
    }

    @Override
    public synchronized void addEvent(String sessionId, RecorderEvent event) {
        RecordingSession session = sessions.get(sessionId);
        if (session != null && session.isActive()) {
            TestStep step = stepBuilderService.build(event, session.getSteps().size() + 1);
            session.getSteps().add(step);
        }
    }

    @Override
    public RecordingSession stopRecording(String sessionId) {
        RecordingSession session = sessions.get(sessionId);
        if (session != null) {
            session.setActive(false);

            TestCase testCase = TestCase.builder()
                    .name(session.getName())
                    .description(session.getDescription() != null ? session.getDescription() : "Recorded from " + session.getTargetUrl())
                    .preconditions(session.getPreconditions())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            testCase = testCaseRepositoryPort.save(testCase);

            for (TestStep step : session.getSteps()) {
                step.setTestCaseId(testCase.getId());
                testStepRepositoryPort.save(step);
            }
        }
        return session;
    }

    @Override
    public RecordingSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    private RecorderEvent convertToEvent(Map<String, Object> data) {
        return RecorderEvent.builder()
                .action((String) data.get("action"))
                .selector((String) data.get("selector"))
                .tagName((String) data.get("tagName"))
                .text((String) data.get("text"))
                .value((String) data.get("value"))
                .attributes((Map<String, String>) data.get("attributes"))
                .build();
    }
}
