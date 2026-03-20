package com.lumitest.testmanagement.domain.port.out;

import com.lumitest.testmanagement.domain.model.TestCase;
import java.util.List;
import java.util.Optional;

public interface TestCaseRepositoryPort {
    TestCase save(TestCase testCase);
    Optional<TestCase> findById(String id);
    List<TestCase> findAll();
    void deleteById(String id);
    
    // Extended queries from legacy repository
    List<TestCase> findByFolder(String folder);
    List<TestCase> findByReleaseId(String releaseId);
    List<TestCase> findByExternalRequirementId(String externalRequirementId);
    List<String> findDistinctFolders();
}
