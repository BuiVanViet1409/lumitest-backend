package com.lumitest.service;

import com.microsoft.playwright.*;
import com.lumitest.automation.PlaywrightAutomationEngine;
import com.lumitest.model.*;
import com.lumitest.repository.ExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class TestExecutionService {

    @Autowired
    private PlaywrightAutomationEngine engine;

    @Autowired
    private ExecutionRepository executionRepo;

    @Async("testTaskExecutor")
    public void runTestCase(TestCase testCase, Execution execution) {
        execution.setStatus("RUNNING");
        execution.setStartTime(LocalDateTime.now());
        execution.setStepResults(new ArrayList<>());
        executionRepo.save(execution);

        String videoDir = "src/main/resources/static/screenshots/" + execution.getId();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            // Cấu hình quay video
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setRecordVideoDir(java.nio.file.Paths.get(videoDir))
                    .setRecordVideoSize(1280, 720));

            Page page = context.newPage();

            boolean allPassed = true;
            for (TestStep step : testCase.getSteps()) {
                StepResult result = engine.executeStep(page, step, execution.getId());
                execution.getStepResults().add(result);
                executionRepo.save(execution);

                if ("FAIL".equals(result.getStatus())) {
                    allPassed = false;
                    break;
                }
            }

            execution.setStatus(allPassed ? "PASSED" : "FAILED");

            // Lấy thông tin video sau khi đóng context
            context.close();
            if (page.video() != null) {
                execution.setVideoPath(page.video().path().getFileName().toString());
            }
            browser.close();

        } catch (Exception e) {
            execution.setStatus("FAILED");
        } finally {
            execution.setEndTime(LocalDateTime.now());
            executionRepo.save(execution);
        }
    }
}
