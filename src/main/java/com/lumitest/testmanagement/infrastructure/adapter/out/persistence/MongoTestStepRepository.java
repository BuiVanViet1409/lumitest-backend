package com.lumitest.testmanagement.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MongoTestStepRepository extends MongoRepository<TestStepEntity, String> {
    List<TestStepEntity> findByTestCaseIdOrderByOrderAsc(String testCaseId);
    void deleteByTestCaseId(String testCaseId);
}
