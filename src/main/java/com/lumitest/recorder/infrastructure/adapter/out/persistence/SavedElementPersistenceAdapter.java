package com.lumitest.recorder.infrastructure.adapter.out.persistence;

import com.lumitest.recorder.domain.model.SavedElement;
import com.lumitest.recorder.domain.port.out.ElementLibraryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SavedElementPersistenceAdapter implements ElementLibraryRepositoryPort {

    private final MongoSavedElementRepository repository;

    @Override
    public SavedElement save(SavedElement element) {
        return mapToDomain(repository.save(mapToEntity(element)));
    }

    @Override
    public Optional<SavedElement> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<SavedElement> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    private SavedElementEntity mapToEntity(SavedElement domain) {
        return SavedElementEntity.builder()
                .id(domain.getId())
                .domain(domain.getDomain())
                .label(domain.getLabel())
                .selector(domain.getSelector())
                .lastUpdated(domain.getLastUpdated())
                .build();
    }

    private SavedElement mapToDomain(SavedElementEntity entity) {
        return SavedElement.builder()
                .id(entity.getId())
                .domain(entity.getDomain())
                .label(entity.getLabel())
                .selector(entity.getSelector())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }
}
