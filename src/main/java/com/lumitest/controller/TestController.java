package com.lumitest.controller;

import com.lumitest.entity.*;
import com.lumitest.repository.TestCaseRepository;
import com.lumitest.service.TestExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private TestCaseRepository testCaseRepo;

    @Autowired
    private TestExecutionService executionService;

    @PostMapping("/testcases")
    public TestCase createTestCase(@RequestBody TestCase testCase) {
        return testCaseRepo.save(testCase);
    }

    @GetMapping("/testcases")
    public List<TestCase> getAllTestCases() {
        return testCaseRepo.findAll();
    }

    @PostMapping("/executions/run/{testCaseId}")
    public ResponseEntity<String> runTest(@PathVariable Long testCaseId) {
        TestCase testCase = testCaseRepo.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("Test case not found"));

        TestExecution execution = new TestExecution();
        execution.setTestCase(testCase);
        execution.setStatus("PENDING");

        executionService.runTestCase(execution);

        return ResponseEntity.ok("Execution started for test case: " + testCase.getName());
    }
}
