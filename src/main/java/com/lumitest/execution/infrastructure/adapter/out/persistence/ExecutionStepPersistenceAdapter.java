package com.lumitest.execution.infrastructure.adapter.out.persistence;

import com.lumitest.execution.domain.model.ExecutionStep;
import com.lumitest.execution.domain.port.out.ExecutionStepRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExecutionStepPersistenceAdapter implements ExecutionStepRepositoryPort {

    private final MongoExecutionStepRepository repository;

    @Override
    public ExecutionStep save(ExecutionStep step) {
        return mapToDomain(repository.save(mapToEntity(step)));
    }

    @Override
    public List<ExecutionStep> findByExecutionIdOrderByStepOrderAsc(String executionId) {
        return repository.findByExecutionIdOrderByStepOrderAsc(executionId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByExecutionId(String executionId) {
        repository.deleteByExecutionId(executionId);
    }

    private ExecutionStepEntity mapToEntity(ExecutionStep domain) {
        return ExecutionStepEntity.builder()
                .id(domain.getId())
                .executionId(domain.getExecutionId())
                .testStepId(domain.getTestStepId())
                .stepOrder(domain.getStepOrder())
                .action(domain.getAction())
                .status(domain.getStatus())
                .screenshotPath(domain.getScreenshotPath())
                .errorMessage(domain.getErrorMessage())
                .duration(domain.getDuration())
                .executedAt(domain.getExecutedAt())
                .build();
    }

    private ExecutionStep mapToDomain(ExecutionStepEntity entity) {
        return ExecutionStep.builder()
                .id(entity.getId())
                .executionId(entity.getExecutionId())
                .testStepId(entity.getTestStepId())
                .stepOrder(entity.getStepOrder())
                .action(entity.getAction())
                .status(entity.getStatus())
                .screenshotPath(entity.getScreenshotPath())
                .errorMessage(entity.getErrorMessage())
                .duration(entity.getDuration())
                .executedAt(entity.getExecutedAt())
                .build();
    }
}
