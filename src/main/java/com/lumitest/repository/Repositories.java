package com.lumitest.repository;

import com.lumitest.model.TestCase;
import com.lumitest.model.Execution;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends MongoRepository<TestCase, String> {}

@Repository
public interface ExecutionRepository extends MongoRepository<Execution, String> {}
