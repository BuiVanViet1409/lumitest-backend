package com.lumitest.controller;

import com.lumitest.model.TestCase;
import com.lumitest.model.TestStep;
import com.lumitest.model.Execution;
import com.lumitest.model.ExecutionStep;
import com.lumitest.service.TestCaseService;
import com.lumitest.service.StepService;
import com.lumitest.service.ExecutionService;
import com.lumitest.service.ScenarioConverterService;
import com.lumitest.repository.ExecutionRepository;
import com.lumitest.repository.ExecutionStepRepository;
import com.lumitest.automation.TestRecorderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final TestCaseService testCaseService;
    private final StepService stepService;
    private final ExecutionService executionService;
    private final ExecutionRepository executionRepo;
    private final ExecutionStepRepository executionStepRepo;
    private final TestRecorderService recorderService;
    private final ScenarioConverterService scenarioConverter;

    // --- TEST CASES ---
    @GetMapping("/testcases")
    public Page<TestCase> getAllTestCases(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "folder", required = false) String folder) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return testCaseService.getAllPaged(pageable, folder);
    }

    @GetMapping("/folders")
    public List<String> getFolders() {
        return testCaseService.getAllFolders();
    }

    @PostMapping("/testcases")
    public TestCase createTestCase(@RequestBody TestCase testCase) {
        return testCaseService.save(testCase);
    }

    @PostMapping("/testcases/{id}/generate")
    public List<TestStep> generateSteps(@PathVariable("id") String id) {
        log.info("Generating test steps for test case ID: {}", id);
        TestCase tc = testCaseService.getById(id);
        // Scenario converter may need adjustment or we use tc.getPreconditions() if it contains URL
        List<TestStep> steps = scenarioConverter.convert(tc.getScenario(), null);
        steps.forEach(s -> s.setTestCaseId(id));
        stepService.saveAll(steps);
        return steps;
    }

    @PostMapping("/testcases/record")
    public List<TestStep> recordTestCase(@RequestParam("url") String url) {
        log.info("Recording test case for URL: {}", url);
        return recorderService.recordSteps(url);
    }

    @PutMapping("/testcases/{id}")
    public TestCase updateTestCase(@PathVariable("id") String id, @RequestBody TestCase testCase) {
        log.info("Updating test case ID: {}", id);
        testCase.setId(id);
        return testCaseService.save(testCase);
    }

    @DeleteMapping("/testcases/{id}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable("id") String id) {
        log.info("Deleting test case ID: {}", id);
        testCaseService.delete(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/testcases/bulk")
    public ResponseEntity<Void> deleteBulkTestCases(@RequestBody List<String> ids) {
        log.info("Bulk deleting {} test cases", ids.size());
        testCaseService.deleteAllByIds(ids);
        return ResponseEntity.ok().build();
    }

    // --- STEPS ---
    @GetMapping("/testcases/{id}/steps")
    public List<TestStep> getSteps(@PathVariable("id") String id) {
        log.info("Fetching steps for test case ID: {}", id);
        return stepService.getStepsByTestCase(id);
    }

    @PostMapping("/testcases/{id}/steps")
    public List<TestStep> addSteps(@PathVariable("id") String id, @RequestBody List<TestStep> steps) {
        log.info("Saving {} steps for test case ID: {}", steps.size(), id);
        // Xóa hết bước cũ của kịch bản này trước khi lưu bộ mới
        stepService.deleteAllByTestCaseId(id);

        steps.forEach(s -> s.setTestCaseId(id));
        stepService.saveAll(steps);
        return stepService.getStepsByTestCase(id);
    }

    @PutMapping("/steps/{stepId}")
    public TestStep updateStep(@PathVariable("stepId") String stepId, @RequestBody TestStep step) {
        step.setId(stepId);
        return stepService.save(step);
    }

    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<Void> deleteStep(@PathVariable("stepId") String stepId) {
        stepService.delete(stepId);
        return ResponseEntity.ok().build();
    }

    // --- EXECUTION ---
    @PostMapping("/testcases/{id}/run")
    public ResponseEntity<Execution> runTest(
            @PathVariable("id") String id,
            @RequestBody(required = false) Map<String, String> options) {
        log.info("Running test case ID: {} with options: {}", id, options);
        TestCase testCase = testCaseService.getById(id);

        Execution execution = new Execution();
        execution.setTestCaseId(id);
        execution.setStatus("PENDING");
        
        if (options != null) {
            execution.setEnvironment(options.get("environment"));
            execution.setConfigProfile(options.get("configProfile"));
            execution.setTestDataProfile(options.get("testDataProfile"));
        }
        
        execution = executionRepo.save(execution);
        executionService.runTest(testCase, execution);

        return ResponseEntity.ok(execution);
    }

    @GetMapping("/testcases/{id}/latest-execution")
    public ResponseEntity<Execution> getLatestExecution(@PathVariable("id") String id) {
        log.info("Fetching latest execution for test case ID: {}", id);
        Execution execution = executionService.getLatestExecutionByTestCase(id);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(execution);
    }

    @GetMapping("/executions/{id}")
    public ResponseEntity<Object> getExecutionDetails(@PathVariable("id") String id) {
        log.info("Fetching execution details for ID: {}", id);
        return executionRepo.findById(id).map(execution -> {
            List<ExecutionStep> steps = executionStepRepo.findByExecutionIdOrderByStepOrderAsc(id);
            Map<String, Object> response = new HashMap<>();
            response.put("execution", execution);
            response.put("steps", steps);
            return ResponseEntity.ok((Object) response);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/video/{executionId}/{filename}")
    public ResponseEntity<Resource> getVideo(
            @PathVariable("executionId") String executionId,
            @PathVariable("filename") String filename) {
        try {
            Path videoPath = Paths.get("src/main/resources/static/screenshots", executionId,
                    filename);
            Resource resource = new UrlResource(
                    videoPath.toUri());
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
            Path path = Paths.get("src/main/resources/static/screenshots", executionId,
                    filename);
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
