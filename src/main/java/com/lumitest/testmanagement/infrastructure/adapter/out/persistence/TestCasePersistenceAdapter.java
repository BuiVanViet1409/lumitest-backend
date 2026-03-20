package com.lumitest.testmanagement.infrastructure.adapter.out.persistence;

import com.lumitest.testmanagement.domain.model.TestCase;
import com.lumitest.testmanagement.domain.port.out.TestCaseRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Persistence adapter for TestCase.
 * Maps between Domain model and MongoDB Entity.
 */
@Component
public class TestCasePersistenceAdapter implements TestCaseRepositoryPort {

    private final MongoTestCaseRepository repository;

    public TestCasePersistenceAdapter(MongoTestCaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public TestCase save(TestCase testCase) {
        TestCaseEntity entity = mapToEntity(testCase);
        return mapToDomain(repository.save(entity));
    }

    @Override
    public Optional<TestCase> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<TestCase> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<TestCase> findByFolder(String folder) {
        // Simple list return, ignoring pagination for now as it's a direct refactor
        return repository.findByFolder(folder).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCase> findByReleaseId(String releaseId) {
        return repository.findByReleaseId(releaseId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCase> findByExternalRequirementId(String externalRequirementId) {
        return repository.findByExternalRequirementId(externalRequirementId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findDistinctFolders() {
        return repository.findDistinctFolders();
    }

    private TestCaseEntity mapToEntity(TestCase testCase) {
        return TestCaseEntity.builder()
                .id(testCase.getId())
                .name(testCase.getName())
                .description(testCase.getDescription())
                .preconditions(testCase.getPreconditions())
                .scenario(testCase.getScenario())
                .folder(testCase.getFolder())
                .acceptanceCriteriaMapping(testCase.getAcceptanceCriteriaMapping())
                .externalRequirementId(testCase.getExternalRequirementId())
                .defectIds(testCase.getDefectIds())
                .riskLevel(testCase.getRiskLevel())
                .priority(testCase.getPriority())
                .businessImpact(testCase.getBusinessImpact())
                .author(testCase.getAuthor())
                .reviewer(testCase.getReviewer())
                .approvalStatus(testCase.getApprovalStatus())
                .releaseId(testCase.getReleaseId())
                .createdAt(testCase.getCreatedAt())
                .build();
    }

    private TestCase mapToDomain(TestCaseEntity entity) {
        return TestCase.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .preconditions(entity.getPreconditions())
                .scenario(entity.getScenario())
                .folder(entity.getFolder())
                .acceptanceCriteriaMapping(entity.getAcceptanceCriteriaMapping())
                .externalRequirementId(entity.getExternalRequirementId())
                .defectIds(entity.getDefectIds())
                .riskLevel(entity.getRiskLevel())
                .priority(entity.getPriority())
                .businessImpact(entity.getBusinessImpact())
                .author(entity.getAuthor())
                .reviewer(entity.getReviewer())
                .approvalStatus(entity.getApprovalStatus())
                .releaseId(entity.getReleaseId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
