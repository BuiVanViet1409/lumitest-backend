package com.lumitest.execution.domain.port.in;

import com.lumitest.execution.domain.model.Execution;
import com.lumitest.execution.domain.model.ExecutionStep;
import com.lumitest.testmanagement.domain.model.TestCase;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RunTestUseCase {
    Execution initiateExecution(String testCaseId, Map<String, String> options);
    void runTest(TestCase testCase, Execution execution);
    Optional<Execution> getLatestExecution(String testCaseId);
    Optional<Execution> getById(String id);
    List<ExecutionStep> getStepsForExecution(String executionId);
}
