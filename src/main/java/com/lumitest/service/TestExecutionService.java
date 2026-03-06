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

    @Async
    public void runTestCase(TestCase testCase, Execution execution) {
        execution.setStatus("RUNNING");
        execution.setStartTime(LocalDateTime.now());
        execution.setStepResults(new ArrayList<>());
        executionRepo.save(execution);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            boolean allPassed = true;
            for (TestStep step : testCase.getSteps()) {
                StepResult result = engine.executeStep(page, step, execution.getId());
                execution.getStepResults().add(result);
                executionRepo.save(execution); // Update progress

                if ("FAIL".equals(result.getStatus())) {
                    allPassed = false;
                    break;
                }
            }

            execution.setStatus(allPassed ? "PASSED" : "FAILED");
            browser.close();

        } catch (Exception e) {
            execution.setStatus("FAILED");
        } finally {
            execution.setEndTime(LocalDateTime.now());
            executionRepo.save(execution);
        }
    }
}
