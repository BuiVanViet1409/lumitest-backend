package com.lumitest.execution.domain.port.out;

import com.lumitest.execution.domain.model.Execution;
import java.util.List;
import java.util.Optional;

public interface ExecutionRepositoryPort {
    Execution save(Execution execution);
    Optional<Execution> findById(String id);
    List<Execution> findAll();
    Optional<Execution> findLatestByTestCaseId(String testCaseId);
    void deleteByTestCaseId(String testCaseId);
}
