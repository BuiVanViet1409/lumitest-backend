package com.lumitest.execution.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MongoExecutionRepository extends MongoRepository<ExecutionEntity, String> {
    Optional<ExecutionEntity> findFirstByTestCaseIdOrderByStartTimeDesc(String testCaseId);
    void deleteByTestCaseId(String testCaseId);
}
