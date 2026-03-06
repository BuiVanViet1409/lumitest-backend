package com.lumitest.repository;

import com.lumitest.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TestCaseRepository extends JpaRepository<TestCase, Long> {
}

@Repository
interface TestStepRepository extends JpaRepository<TestStep, Long> {
}

@Repository
interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {
}

@Repository
interface StepResultRepository extends JpaRepository<StepResult, Long> {
}
