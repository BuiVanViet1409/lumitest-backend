package com.lumitest.controller;

import com.lumitest.model.*;
import com.lumitest.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private TestCaseService testCaseService;

    @Autowired
    private StepService stepService;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private com.lumitest.repository.ExecutionRepository executionRepo;

    @Autowired
    private com.lumitest.repository.ExecutionStepRepository executionStepRepo;

    @Autowired
    private com.lumitest.automation.TestRecorderService recorderService;

    @Autowired
    private ScenarioConverterService scenarioConverter;

    // --- TEST CASES ---
    @GetMapping("/testcases")
    public List<TestCase> getAllTestCases() {
        return testCaseService.getAll();
    }

    @PostMapping("/testcases")
    public TestCase createTestCase(@RequestBody TestCase testCase) {
        return testCaseService.save(testCase);
    }

    @PostMapping("/testcases/{id}/generate")
    public List<TestStep> generateSteps(@PathVariable String id) {
        TestCase tc = testCaseService.getById(id);
        List<TestStep> steps = scenarioConverter.convert(tc.getScenario(), tc.getApplicationUrl());
        steps.forEach(s -> s.setTestCaseId(id));
        stepService.saveAll(steps);
        return steps;
    }

    @PostMapping("/testcases/record")
    public List<TestStep> recordTestCase(@RequestParam String url) {
        return recorderService.recordSteps(url);
    }

    @PutMapping("/testcases/{id}")
    public TestCase updateTestCase(@PathVariable String id, @RequestBody TestCase testCase) {
        testCase.setId(id);
        return testCaseService.save(testCase);
    }

    @DeleteMapping("/testcases/{id}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable String id) {
        testCaseService.delete(id);
        return ResponseEntity.ok().build();
    }

    // --- STEPS ---
    @GetMapping("/testcases/{id}/steps")
    public List<TestStep> getSteps(@PathVariable String id) {
        return stepService.getStepsByTestCase(id);
    }

    @PostMapping("/testcases/{id}/steps")
    public List<TestStep> addSteps(@PathVariable String id, @RequestBody List<TestStep> steps) {
        // Xóa hết bước cũ của kịch bản này trước khi lưu bộ mới
        stepService.deleteAllByTestCaseId(id);

        steps.forEach(s -> s.setTestCaseId(id));
        stepService.saveAll(steps);
        return stepService.getStepsByTestCase(id);
    }

    @PutMapping("/steps/{stepId}")
    public TestStep updateStep(@PathVariable String stepId, @RequestBody TestStep step) {
        step.setId(stepId);
        return stepService.save(step);
    }

    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<Void> deleteStep(@PathVariable String stepId) {
        stepService.delete(stepId);
        return ResponseEntity.ok().build();
    }

    // --- EXECUTION ---
    @PostMapping("/testcases/{id}/run")
    public ResponseEntity<Execution> runTest(@PathVariable String id) {
        TestCase testCase = testCaseService.getById(id);

        Execution execution = new Execution();
        execution.setTestCaseId(id);
        execution.setStatus("PENDING");
        execution = executionRepo.save(execution);

        executionService.runTest(testCase, execution);

        return ResponseEntity.ok(execution);
    }

    @GetMapping("/testcases/{id}/latest-execution")
    public Execution getLatestExecution(@PathVariable String id) {
        return executionService.getLatestExecutionByTestCase(id);
    }

    @GetMapping("/executions/{id}")
    public ResponseEntity<Object> getExecutionDetails(@PathVariable String id) {
        return executionRepo.findById(id).map(execution -> {
            List<ExecutionStep> steps = executionStepRepo.findByExecutionIdOrderByStepOrderAsc(id);
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("execution", execution);
            response.put("steps", steps);
            return ResponseEntity.ok((Object) response);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/video/{executionId}/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> getVideo(@PathVariable String executionId,
            @PathVariable String filename) {
        try {
            java.nio.file.Path videoPath = java.nio.file.Paths.get("src/main/resources/static/screenshots", executionId,
                    filename);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(
                    videoPath.toUri());
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType("video/webm"))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/screenshots/{executionId}/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> getScreenshot(@PathVariable String executionId,
            @PathVariable String filename) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("src/main/resources/static/screenshots", executionId,
                    filename);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.IMAGE_PNG)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
