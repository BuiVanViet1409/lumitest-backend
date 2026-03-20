package com.lumitest.execution.domain.port.out;

import com.lumitest.execution.domain.model.ExecutionStep;
import java.util.List;

public interface ExecutionStepRepositoryPort {
    ExecutionStep save(ExecutionStep step);
    List<ExecutionStep> findByExecutionIdOrderByStepOrderAsc(String executionId);
    void deleteByExecutionId(String executionId);
}
