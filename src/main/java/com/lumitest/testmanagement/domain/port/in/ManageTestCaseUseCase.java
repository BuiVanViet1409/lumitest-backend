package com.lumitest.testmanagement.domain.port.in;

import com.lumitest.testmanagement.domain.model.Release;
import com.lumitest.testmanagement.domain.model.TestCase;
import com.lumitest.testmanagement.domain.model.TestStep;
import java.util.List;
import java.util.Optional;

public interface ManageTestCaseUseCase {
    TestCase createTestCase(TestCase testCase);
    TestCase updateTestCase(String id, TestCase testCase);
    Optional<TestCase> getTestCase(String id);
    List<TestCase> getAllTestCases();
    void deleteTestCase(String id);
    void deleteStep(String id);
    
    List<String> getAllFolders();
    List<TestCase> getByFolder(String folder);
    TestStep saveStep(TestStep step);
    List<TestStep> getStepsForTestCase(String testCaseId);
    
    List<Release> getAllReleases();
    Optional<Release> getRelease(String id);
    Release saveRelease(Release release);
    void deleteRelease(String id);
}
