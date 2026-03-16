package com.lumitest.repository;

import com.lumitest.model.ExecutionStep;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExecutionStepRepository extends MongoRepository<ExecutionStep, String> {
    List<ExecutionStep> findByExecutionIdOrderByStepOrderAsc(String executionId);

    void deleteByExecutionId(String executionId);
}
