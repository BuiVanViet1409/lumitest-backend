package com.lumitest.service;

import com.microsoft.playwright.*;
import com.lumitest.automation.PlaywrightAutomationEngine;
import com.lumitest.entity.*;
import com.lumitest.repository.*;
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
    private TestExecutionRepository executionRepo;

    @Autowired
    private StepResultRepository stepResultRepo;

    @Async
    public void runTestCase(TestExecution execution) {
        execution.setStatus("RUNNING");
        execution.setStartTime(LocalDateTime.now());
        executionRepo.save(execution);

        if (execution.getStepResults() == null) {
            execution.setStepResults(new ArrayList<>());
        }

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            boolean allPassed = true;
            for (TestStep step : execution.getTestCase().getSteps()) {
                StepResult result = engine.executeStep(page, step,
                        "src/main/resources/static/screenshots/" + execution.getId());
                result.setExecution(execution);
                stepResultRepo.save(result);
                execution.getStepResults().add(result);

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
