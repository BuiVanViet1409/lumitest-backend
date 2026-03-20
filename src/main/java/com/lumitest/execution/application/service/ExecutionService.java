package com.lumitest.execution.application.service;

import com.lumitest.execution.domain.model.Execution;
import com.lumitest.execution.domain.model.ExecutionStep;
import com.lumitest.execution.domain.port.in.RunTestUseCase;
import com.lumitest.execution.domain.port.out.AutomationRunnerPort;
import com.lumitest.execution.domain.port.out.ExecutionRepositoryPort;
import com.lumitest.execution.domain.port.out.ExecutionStepRepositoryPort;
import com.lumitest.testmanagement.domain.model.TestCase;
import com.lumitest.testmanagement.domain.model.TestStep;
import com.lumitest.testmanagement.domain.port.out.TestStepRepositoryPort;
import com.lumitest.config.LumiTestConfig;
import com.lumitest.util.PathUtils;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionService implements RunTestUseCase {

    private final ExecutionRepositoryPort executionRepositoryPort;
    private final ExecutionStepRepositoryPort executionStepRepositoryPort;
    private final TestStepRepositoryPort testStepRepositoryPort;
    private final AutomationRunnerPort automationRunnerPort;
    private final LumiTestConfig config;

    @Override
    public Execution initiateExecution(String testCaseId, Map<String, String> options) {
        Execution execution = Execution.builder()
                .testCaseId(testCaseId)
                .status("PENDING")
                .environment(options != null ? options.get("environment") : null)
                .configProfile(options != null ? options.get("configProfile") : null)
                .testDataProfile(options != null ? options.get("testDataProfile") : null)
                .build();
        return executionRepositoryPort.save(execution);
    }

    @Override
    @Async("testTaskExecutor")
    public void runTest(TestCase testCase, Execution execution) {
        execution.setStatus("RUNNING");
        execution.setStartTime(LocalDateTime.now());
        executionRepositoryPort.save(execution);

        List<TestStep> steps = testStepRepositoryPort.findByTestCaseId(testCase.getId());

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

            for (int i = 0; i < steps.size(); i++) {
                TestStep step = steps.get(i);
                String msg = "Step " + (i + 1) + "/" + steps.size() + ": " + step.getDescription();
                
                execution.setProgressMessage(msg);
                executionRepositoryPort.save(execution);

                ExecutionStep execStep = automationRunnerPort.executeStep(page, step, execution.getId());
                executionStepRepositoryPort.save(execStep);

                if ("FAILED".equals(execStep.getStatus())) {
                    allPassed = false;
                    break;
                }
            }

            execution.setStatus(allPassed ? "PASSED" : "FAILED");
            execution.setProgressMessage(allPassed ? "Test completed successfully!" : "Test failed!");

            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

            if (page.video() != null) {
                execution.setVideoPath(PathUtils.getVideoName(page.video()));
            }
            context.close();
            browser.close();

        } catch (Exception e) {
            execution.setStatus("FAILED");
            log.error("Execution failed", e);
        } finally {
            execution.setEndTime(LocalDateTime.now());
            executionRepositoryPort.save(execution);
        }
    }

    public Optional<Execution> getLatestExecution(String testCaseId) {
        return executionRepositoryPort.findLatestByTestCaseId(testCaseId);
    }

    public Optional<Execution> getById(String id) {
        return executionRepositoryPort.findById(id);
    }

    public List<ExecutionStep> getStepsForExecution(String executionId) {
        return executionStepRepositoryPort.findByExecutionIdOrderByStepOrderAsc(executionId);
    }
}
