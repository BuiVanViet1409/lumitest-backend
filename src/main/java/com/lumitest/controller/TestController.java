package com.lumitest.controller;

import com.lumitest.model.*;
import com.lumitest.repository.TestCaseRepository;
import com.lumitest.repository.ExecutionRepository;
import com.lumitest.service.TestExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private TestCaseRepository testCaseRepo;

    @Autowired
    private ExecutionRepository executionRepo;

    @Autowired
    private TestExecutionService executionService;

    @Autowired
    private com.lumitest.automation.TestRecorderService recorderService;

    @PostMapping("/testcases")
    public TestCase createTestCase(@RequestBody TestCase testCase) {
        if (testCase.getSteps() == null)
            testCase.setSteps(new ArrayList<>());
        return testCaseRepo.save(testCase);
    }

    @GetMapping("/testcases")
    public List<TestCase> getAllTestCases() {
        return testCaseRepo.findAll();
    }

    @PostMapping("/testcases/{id}/steps")
    public TestCase addSteps(@PathVariable String id, @RequestBody List<TestStep> steps) {
        TestCase tc = testCaseRepo.findById(id).orElseThrow();
        tc.getSteps().addAll(steps);
        return testCaseRepo.save(tc);
    }

    @PostMapping("/executions/run/{testCaseId}")
    public ResponseEntity<Execution> runTest(@PathVariable String testCaseId) {
        TestCase testCase = testCaseRepo.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("Test case not found"));

        Execution execution = new Execution();
        execution.setTestCaseId(testCaseId);
        execution.setStatus("PENDING");
        execution = executionRepo.save(execution);

        executionService.runTestCase(testCase, execution);

        return ResponseEntity.ok(execution);
    }

    @GetMapping("/executions/{executionId}")
    public Execution getExecution(@PathVariable String executionId) {
        return executionRepo.findById(executionId).orElseThrow();
    }

    @GetMapping("/executions/{executionId}/report")
    public ResponseEntity<String> getReport(@PathVariable String executionId) {
        Execution ex = executionRepo.findById(executionId).orElseThrow();
        StringBuilder report = new StringBuilder();
        report.append("BÁO CÁO KẾT QUẢ KIỂM THỬ\n");
        report.append("Trạng thái tổng quát: ").append(ex.getStatus().equals("PASSED") ? "THÀNH CÔNG" : "THẤT BẠI")
                .append("\n\n");

        if (ex.getStepResults() != null) {
            for (StepResult res : ex.getStepResults()) {
                report.append("Bước ").append(res.getStepOrder())
                        .append(" - ").append(res.getStatus().equals("PASS") ? "ĐẠT" : "KHÔNG ĐẠT")
                        .append(" - Ảnh bằng chứng: ").append(res.getScreenshotPath()).append("\n");
            }
        }

        if (ex.getVideoPath() != null) {
            report.append("\nVideo quay lại: ").append(ex.getVideoPath()).append("\n");
        }

        return ResponseEntity.ok(report.toString());
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

    @PostMapping("/testcases/record")
    public List<TestStep> recordTestCase(@RequestParam String url) {
        return recorderService.recordSteps(url);
    }
}
