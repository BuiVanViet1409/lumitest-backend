package com.lumitest.testmanagement.infrastructure.adapter.out.persistence;

import com.lumitest.testmanagement.domain.model.TestStep;
import com.lumitest.testmanagement.domain.port.out.TestStepRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestStepPersistenceAdapter implements TestStepRepositoryPort {

    private final MongoTestStepRepository repository;

    public TestStepPersistenceAdapter(MongoTestStepRepository repository) {
        this.repository = repository;
    }

    @Override
    public TestStep save(TestStep testStep) {
        return mapToDomain(repository.save(mapToEntity(testStep)));
    }

    @Override
    public List<TestStep> findByTestCaseId(String testCaseId) {
        return repository.findByTestCaseIdOrderByOrderAsc(testCaseId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByTestCaseId(String testCaseId) {
        repository.deleteByTestCaseId(testCaseId);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    private TestStepEntity mapToEntity(TestStep step) {
        return TestStepEntity.builder()
                .id(step.getId())
                .testCaseId(step.getTestCaseId())
                .order(step.getOrder())
                .description(step.getDescription())
                .testData(step.getTestData())
                .expectedResult(step.getExpectedResult())
                .verificationType(step.getVerificationType())
                .verificationRule(step.getVerificationRule())
                .status(step.getStatus())
                .retryCount(step.getRetryCount())
                .build();
    }

    private TestStep mapToDomain(TestStepEntity entity) {
        return TestStep.builder()
                .id(entity.getId())
                .testCaseId(entity.getTestCaseId())
                .order(entity.getOrder())
                .description(entity.getDescription())
                .testData(entity.getTestData())
                .expectedResult(entity.getExpectedResult())
                .verificationType(entity.getVerificationType())
                .verificationRule(entity.getVerificationRule())
                .status(entity.getStatus())
                .retryCount(entity.getRetryCount())
                .build();
    }
}
