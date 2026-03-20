package com.lumitest.execution.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MongoExecutionStepRepository extends MongoRepository<ExecutionStepEntity, String> {
    List<ExecutionStepEntity> findByExecutionIdOrderByStepOrderAsc(String executionId);
    void deleteByExecutionId(String executionId);
}
