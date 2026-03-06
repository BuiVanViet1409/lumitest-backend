package com.lumitest.repository;

import com.lumitest.model.TestStep;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestStepRepository extends MongoRepository<TestStep, String> {
    List<TestStep> findByTestCaseIdOrderByOrderAsc(String testCaseId);

    void deleteByTestCaseId(String testCaseId);

    long countByTestCaseId(String testCaseId);
}
