package com.lumitest.service;

import com.microsoft.playwright.*;
import com.lumitest.automation.AutomationRunnerService;
import com.lumitest.model.*;
import com.lumitest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.lumitest.constant.TestConstants;
import com.lumitest.util.PathUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExecutionService {

    @Autowired
    private AutomationRunnerService runner;

    @Autowired
    private ExecutionRepository executionRepo;

    @Autowired
    private ExecutionStepRepository executionStepRepo;

    @Autowired
    private TestStepRepository testStepRepo;

    @Autowired
    private com.lumitest.config.LumiTestConfig config;

    @Async("testTaskExecutor")
    public void runTest(TestCase testCase, Execution execution) {
        execution.setStatus(TestConstants.Status.RUNNING);
        execution.setStartTime(LocalDateTime.now());
        executionRepo.save(execution);

        List<TestStep> steps = testStepRepo.findByTestCaseIdOrderByOrderAsc(testCase.getId());

        // Check limit
        if (steps.size() > config.getMaxTestSteps()) {
            steps = steps.subList(0, config.getMaxTestSteps());
        }

        String videoDir = PathUtils.getExecutionDir(config.getScreenshotPath(), execution.getId());

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(config.getBrowser().isHeadless()));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setRecordVideoDir(java.nio.file.Paths.get(videoDir))
                    .setRecordVideoSize(1280, 720));

            Page page = context.newPage();
            page.setDefaultTimeout(config.getTimeouts().getGlobal());
            boolean allPassed = true;

            log.info("🚀 Starting test execution for: {}", testCase.getName());
            log.info("🌐 Target URL: {}", testCase.getApplicationUrl());

            for (int i = 0; i < steps.size(); i++) {
                TestStep step = steps.get(i);
                String msg = "Step " + (i + 1) + "/" + steps.size() + ": " + step.getAction() + " ["
                        + step.getSelector() + "]";
                log.info("👉 {}", msg);

                execution.setProgressMessage(msg);
                executionRepo.save(execution);

                ExecutionStep execStep = runner.executeStep(page, step, execution.getId());
                executionStepRepo.save(execStep);

                if (TestConstants.Status.FAIL.equals(execStep.getStatus())) {
                    log.error("❌ Step FAILED: {}", execStep.getErrorMessage());
                    allPassed = false;
                    break;
                } else {
                    log.info("✅ Step PASSED");
                }
            }

            execution.setStatus(allPassed ? TestConstants.Status.PASSED : TestConstants.Status.FAILED);
            execution.setProgressMessage(allPassed ? "Test completed successfully!" : "Test failed!");

            // Chờ 2 giây để video kịp ghi lại kết quả cuối cùng trước khi đóng
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
            }

            context.close();

            if (page.video() != null) {
                execution.setVideoPath(PathUtils.getVideoName(page.video()));
            }
            browser.close();

        } catch (Exception e) {
            execution.setStatus(TestConstants.Status.FAILED);
            e.printStackTrace();
        } finally {
            execution.setEndTime(LocalDateTime.now());
            executionRepo.save(execution);
        }
    }

    public Execution getLatestExecutionByTestCase(String testCaseId) {
        return executionRepo.findFirstByTestCaseIdOrderByStartTimeDesc(testCaseId).orElse(null);
    }
}
