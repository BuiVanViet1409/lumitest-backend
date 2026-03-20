package com.lumitest.testmanagement.domain.port.out;

import com.lumitest.testmanagement.domain.model.TestStep;
import java.util.List;

public interface TestStepRepositoryPort {
    TestStep save(TestStep testStep);
    List<TestStep> findByTestCaseId(String testCaseId);
    void deleteByTestCaseId(String testCaseId);
    void deleteById(String id);
}
