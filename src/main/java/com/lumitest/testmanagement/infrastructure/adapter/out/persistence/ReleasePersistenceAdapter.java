package com.lumitest.testmanagement.infrastructure.adapter.out.persistence;

import com.lumitest.testmanagement.domain.model.Release;
import com.lumitest.testmanagement.domain.port.out.ReleaseRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ReleasePersistenceAdapter implements ReleaseRepositoryPort {

    private final MongoReleaseRepository repository;

    public ReleasePersistenceAdapter(MongoReleaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Release save(Release release) {
        return mapToDomain(repository.save(mapToEntity(release)));
    }

    @Override
    public Optional<Release> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Release> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    private ReleaseEntity mapToEntity(Release release) {
        return ReleaseEntity.builder()
                .id(release.getId())
                .name(release.getName())
                .description(release.getDescription())
                .status(release.getStatus())
                .startDate(release.getStartDate())
                .endDate(release.getEndDate())
                .createdAt(release.getCreatedAt())
                .createdBy(release.getCreatedBy())
                .build();
    }

    private Release mapToDomain(ReleaseEntity entity) {
        return Release.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
