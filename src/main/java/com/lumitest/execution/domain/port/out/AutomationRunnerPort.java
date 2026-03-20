package com.lumitest.execution.domain.port.out;

import com.lumitest.execution.domain.model.ExecutionStep;
import com.lumitest.testmanagement.domain.model.TestStep;
import com.microsoft.playwright.Page;

public interface AutomationRunnerPort {
    ExecutionStep executeStep(Page page, TestStep step, String executionId);
}
