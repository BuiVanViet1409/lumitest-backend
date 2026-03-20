package com.lumitest.execution.infrastructure.adapter.out.automation;

import com.lumitest.execution.domain.model.ExecutionStep;
import com.lumitest.execution.domain.port.out.AutomationRunnerPort;
import com.lumitest.testmanagement.domain.model.TestStep;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class PlaywrightAutomationAdapter implements AutomationRunnerPort {

    @Override
    public ExecutionStep executeStep(Page page, TestStep step, String executionId) {
        log.info("Executing step: {} for execution: {}", step.getDescription(), executionId);
        String status = "PASSED";
        String error = null;
        long startTime = System.currentTimeMillis();

        try {
            switch (step.getDescription().toUpperCase()) {
                case "CLICK" -> page.click(step.getTestData());
                case "INPUT_TEXT" -> page.fill(step.getTestData(), step.getTestData());
                case "WAIT" -> page.waitForTimeout(Double.parseDouble(step.getTestData()));
                case "ASSERT_TEXT" -> {
                    if (!page.textContent("body").contains(step.getTestData())) {
                        status = "FAILED";
                        error = "Text not found: " + step.getTestData();
                    }
                }
                default -> {
                    if (step.getDescription().toUpperCase().contains("CLICK")) {
                        String selector = extractSelector(step.getDescription());
                        page.click(selector);
                    } else if (step.getDescription().toUpperCase().contains("INPUT")) {
                         // Similar logic...
                    }
                }
            }
        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            log.error("Step execution failed", e);
        }

        return ExecutionStep.builder()
                .executionId(executionId)
                .testStepId(step.getId())
                .status(status)
                .errorMessage(error)
                .duration(System.currentTimeMillis() - startTime)
                .executedAt(LocalDateTime.now())
                .build();
    }

    private String extractSelector(String description) {
        if (description.contains("on ")) {
            return description.substring(description.indexOf("on ") + 3).trim();
        }
        return description;
    }
}
