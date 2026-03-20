package com.lumitest.testmanagement.application.service;

import com.lumitest.testmanagement.domain.model.Release;
import com.lumitest.testmanagement.domain.model.TestCase;
import com.lumitest.testmanagement.domain.model.TestStep;
import com.lumitest.testmanagement.domain.port.in.ManageTestCaseUseCase;
import com.lumitest.testmanagement.domain.port.out.ReleaseRepositoryPort;
import com.lumitest.testmanagement.domain.port.out.TestCaseRepositoryPort;
import com.lumitest.testmanagement.domain.port.out.TestStepRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestCaseService implements ManageTestCaseUseCase {

    private final TestCaseRepositoryPort testCaseRepositoryPort;
    private final TestStepRepositoryPort testStepRepositoryPort;
    private final ReleaseRepositoryPort releaseRepositoryPort;

    @Override
    public List<TestCase> getAllTestCases() {
        List<TestCase> testCases = testCaseRepositoryPort.findAll();
        testCases.forEach(tc -> tc.setStepsCount(countSteps(tc.getId())));
        return testCases;
    }

    @Override
    public Optional<TestCase> getTestCase(String id) {
        return testCaseRepositoryPort.findById(id)
                .map(tc -> {
                    tc.setStepsCount(countSteps(id));
                    return tc;
                });
    }

    @Override
    public TestCase createTestCase(TestCase testCase) {
        return testCaseRepositoryPort.save(testCase);
    }

    @Override
    public TestCase updateTestCase(String id, TestCase testCase) {
        testCase.setId(id);
        return testCaseRepositoryPort.save(testCase);
    }

    @Override
    public void deleteTestCase(String id) {
        testStepRepositoryPort.deleteByTestCaseId(id);
        testCaseRepositoryPort.deleteById(id);
    }
    
    public List<String> getAllFolders() {
        return testCaseRepositoryPort.findDistinctFolders();
    }
    
    public List<TestCase> getByFolder(String folder) {
        return testCaseRepositoryPort.findByFolder(folder);
    }

    private long countSteps(String testCaseId) {
        return testStepRepositoryPort.findByTestCaseId(testCaseId).size();
    }
    
    public TestStep saveStep(TestStep step) {
        return testStepRepositoryPort.save(step);
    }

    @Override
    public void deleteStep(String id) {
        testStepRepositoryPort.deleteById(id);
    }
    
    public List<TestStep> getStepsForTestCase(String testCaseId) {
        return testStepRepositoryPort.findByTestCaseId(testCaseId);
    }

    // --- Release Management ---

    public List<Release> getAllReleases() {
        return releaseRepositoryPort.findAll();
    }

    public Optional<Release> getRelease(String id) {
        return releaseRepositoryPort.findById(id);
    }

    public Release saveRelease(Release release) {
        return releaseRepositoryPort.save(release);
    }

    public void deleteRelease(String id) {
        releaseRepositoryPort.deleteById(id);
    }
}
