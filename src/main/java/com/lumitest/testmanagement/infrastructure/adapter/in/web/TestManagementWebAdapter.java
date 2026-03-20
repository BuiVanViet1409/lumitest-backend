package com.lumitest.testmanagement.infrastructure.adapter.in.web;

import com.lumitest.testmanagement.domain.model.Release;
import com.lumitest.testmanagement.domain.model.TestCase;
import com.lumitest.testmanagement.domain.model.TestStep;
import com.lumitest.testmanagement.domain.port.in.ManageTestCaseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestManagementWebAdapter {

    private final ManageTestCaseUseCase manageTestCaseUseCase;

    @GetMapping("/testcases")
    public Page<TestCase> getAllTestCases(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "folder", required = false) String folder) {
        
        List<TestCase> allCases;
        if (folder != null && !folder.isEmpty() && !"All".equalsIgnoreCase(folder)) {
            allCases = manageTestCaseUseCase.getByFolder(folder);
        } else {
            allCases = manageTestCaseUseCase.getAllTestCases();
        }
        
        int start = Math.min(page * size, allCases.size());
        int end = Math.min((page + 1) * size, allCases.size());
        List<TestCase> pagedList = allCases.subList(start, end);
        
        return new PageImpl<>(pagedList, PageRequest.of(page, size), allCases.size());
    }

    @GetMapping("/folders")
    public List<String> getFolders() {
        return manageTestCaseUseCase.getAllFolders();
    }

    @PostMapping("/testcases")
    public TestCase createTestCase(@RequestBody TestCase testCase) {
        return manageTestCaseUseCase.createTestCase(testCase);
    }

    @GetMapping("/testcases/{id}")
    public ResponseEntity<TestCase> getTestCase(@PathVariable String id) {
        return manageTestCaseUseCase.getTestCase(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/testcases/{id}")
    public TestCase updateTestCase(@PathVariable String id, @RequestBody TestCase testCase) {
        return manageTestCaseUseCase.updateTestCase(id, testCase);
    }

    @DeleteMapping("/testcases/{id}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable String id) {
        manageTestCaseUseCase.deleteTestCase(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/testcases/{id}/steps")
    public List<TestStep> getSteps(@PathVariable String id) {
        return manageTestCaseUseCase.getStepsForTestCase(id);
    }

    @PostMapping("/testcases/{id}/steps")
    public List<TestStep> saveSteps(@PathVariable String id, @RequestBody List<TestStep> steps) {
        steps.forEach(s -> {
            s.setTestCaseId(id);
            manageTestCaseUseCase.saveStep(s);
        });
        return manageTestCaseUseCase.getStepsForTestCase(id);
    }

    @DeleteMapping("/steps/{id}")
    public ResponseEntity<Void> deleteStep(@PathVariable String id) {
        manageTestCaseUseCase.deleteStep(id);
        return ResponseEntity.noContent().build();
    }

    // --- Release Management ---

    @GetMapping("/releases")
    public List<Release> getAllReleases() {
        return manageTestCaseUseCase.getAllReleases();
    }

    @PostMapping("/releases")
    public Release createRelease(@RequestBody Release release) {
        return manageTestCaseUseCase.saveRelease(release);
    }

    @GetMapping("/releases/{id}")
    public ResponseEntity<Release> getRelease(@PathVariable String id) {
        return manageTestCaseUseCase.getRelease(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/releases/{id}")
    public ResponseEntity<Void> deleteRelease(@PathVariable String id) {
        manageTestCaseUseCase.deleteRelease(id);
        return ResponseEntity.noContent().build();
    }
}
