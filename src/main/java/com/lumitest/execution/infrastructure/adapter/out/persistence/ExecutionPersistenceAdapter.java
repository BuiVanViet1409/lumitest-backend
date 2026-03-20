package com.lumitest.execution.infrastructure.adapter.out.persistence;

import com.lumitest.execution.domain.model.Execution;
import com.lumitest.execution.domain.port.out.ExecutionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExecutionPersistenceAdapter implements ExecutionRepositoryPort {

    private final MongoExecutionRepository repository;

    @Override
    public Execution save(Execution execution) {
        return mapToDomain(repository.save(mapToEntity(execution)));
    }

    @Override
    public Optional<Execution> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Execution> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Execution> findLatestByTestCaseId(String testCaseId) {
        return repository.findFirstByTestCaseIdOrderByStartTimeDesc(testCaseId).map(this::mapToDomain);
    }

    @Override
    public void deleteByTestCaseId(String testCaseId) {
        repository.deleteByTestCaseId(testCaseId);
    }

    private ExecutionEntity mapToEntity(Execution domain) {
        return ExecutionEntity.builder()
                .id(domain.getId())
                .testCaseId(domain.getTestCaseId())
                .status(domain.getStatus())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .videoPath(domain.getVideoPath())
                .progressMessage(domain.getProgressMessage())
                .environment(domain.getEnvironment())
                .configProfile(domain.getConfigProfile())
                .testDataProfile(domain.getTestDataProfile())
                .build();
    }

    private Execution mapToDomain(ExecutionEntity entity) {
        return Execution.builder()
                .id(entity.getId())
                .testCaseId(entity.getTestCaseId())
                .status(entity.getStatus())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .videoPath(entity.getVideoPath())
                .progressMessage(entity.getProgressMessage())
                .environment(entity.getEnvironment())
                .configProfile(entity.getConfigProfile())
                .testDataProfile(entity.getTestDataProfile())
                .build();
    }
}
