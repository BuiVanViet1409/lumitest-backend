package com.lumitest.execution.infrastructure.adapter.in.web;

import com.lumitest.execution.domain.port.in.RunTestUseCase;
import com.lumitest.execution.domain.model.Execution;
import com.lumitest.execution.domain.model.ExecutionStep;
import com.lumitest.testmanagement.domain.port.in.ManageTestCaseUseCase;
import com.lumitest.testmanagement.domain.model.TestCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExecutionWebAdapter {

    private final RunTestUseCase runTestUseCase;
    private final ManageTestCaseUseCase testCaseUseCase;

    @PostMapping("/testcases/{id}/run")
    public ResponseEntity<Execution> runTest(
            @PathVariable("id") String id,
            @RequestBody(required = false) Map<String, String> options) {
        
        log.info("Running test case ID: {} with options: {}", id, options);
        
        TestCase testCase = testCaseUseCase.getTestCase(id)
                .orElseThrow(() -> new RuntimeException("Test case not found"));

        Execution execution = runTestUseCase.initiateExecution(id, options);
        runTestUseCase.runTest(testCase, execution);

        return ResponseEntity.ok(execution);
    }

    @GetMapping("/testcases/{id}/latest-execution")
    public ResponseEntity<Execution> getLatestExecution(@PathVariable("id") String id) {
        return runTestUseCase.getLatestExecution(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/executions/{id}")
    public ResponseEntity<Map<String, Object>> getExecutionDetails(@PathVariable("id") String id) {
        return runTestUseCase.getById(id).map(execution -> {
            List<ExecutionStep> steps = runTestUseCase.getStepsForExecution(id);
            Map<String, Object> response = new HashMap<>();
            response.put("execution", execution);
            response.put("steps", steps);
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/video/{executionId}/{filename}")
    public ResponseEntity<Resource> getVideo(
            @PathVariable("executionId") String executionId,
            @PathVariable("filename") String filename) {
        try {
            Path videoPath = Paths.get("src/main/resources/static/screenshots", executionId, filename);
            Resource resource = new UrlResource(videoPath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/webm"))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/screenshots/{executionId}/{filename}")
    public ResponseEntity<Resource> getScreenshot(
            @PathVariable("executionId") String executionId,
            @PathVariable("filename") String filename) {
        try {
            Path path = Paths.get("src/main/resources/static/screenshots", executionId, filename);
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
