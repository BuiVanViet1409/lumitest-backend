package com.lumitest.repository;

import com.lumitest.model.Execution;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExecutionRepository extends MongoRepository<Execution, String> {
    Optional<Execution> findFirstByTestCaseIdOrderByStartTimeDesc(String testCaseId);
}
