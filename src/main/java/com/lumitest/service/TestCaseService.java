package com.lumitest.service;

import com.lumitest.model.TestCase;
import com.lumitest.model.Execution;
import com.lumitest.repository.TestCaseRepository;
import com.lumitest.repository.TestStepRepository;
import com.lumitest.repository.ExecutionRepository;
import com.lumitest.repository.ExecutionStepRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestCaseService {
    private final TestCaseRepository testCaseRepo;
    private final TestStepRepository stepRepo;
    private final ExecutionRepository executionRepo;
    private final ExecutionStepRepository executionStepRepo;

    public List<TestCase> getAll() {
        List<TestCase> list = testCaseRepo.findAll();
        list.forEach(tc -> tc.setStepsCount(stepRepo.countByTestCaseId(tc.getId())));
        return list;
    }

    public Page<TestCase> getAllPaged(Pageable pageable, String folder) {
        Page<TestCase> page;
        if (folder != null && !folder.isEmpty() && !"All".equalsIgnoreCase(folder)) {
            page = testCaseRepo.findByFolder(folder, pageable);
        } else {
            page = testCaseRepo.findAll(pageable);
        }
        // Optimization: Page limits the N, so N+1 is less impactful here,
        // but we still process only the items in the current page.
        page.getContent().forEach(tc -> tc.setStepsCount(stepRepo.countByTestCaseId(tc.getId())));
        return page;
    }

    public List<String> getAllFolders() {
        return testCaseRepo.findDistinctFolders();
    }

    public TestCase getById(String id) {
        TestCase tc = testCaseRepo.findById(id).orElseThrow();
        tc.setStepsCount(stepRepo.countByTestCaseId(id));
        return tc;
    }

    public TestCase save(TestCase tc) {
        return testCaseRepo.save(tc);
    }

    public void delete(String id) {
        // 1. Delete Test Steps
        stepRepo.deleteByTestCaseId(id);

        // 2. Delete Execution Steps (via Executions)
        // Note: MongoDB doesn't support joins, so we find all execution IDs first
        List<Execution> executions = executionRepo.findAll().stream()
                .filter(e -> id.equals(e.getTestCaseId()))
                .collect(Collectors.toList());

        for (Execution exec : executions) {
            executionStepRepo.deleteByExecutionId(exec.getId());
        }

        // 3. Delete Executions
        executionRepo.deleteByTestCaseId(id);

        // 4. Delete the Test Case itself
        testCaseRepo.deleteById(id);
    }

    public void deleteAllByIds(List<String> ids) {
        for (String id : ids) {
            delete(id);
        }
    }

    public com.lumitest.model.TestStep saveStep(com.lumitest.model.TestStep step) {
        return stepRepo.save(step);
    }
}
