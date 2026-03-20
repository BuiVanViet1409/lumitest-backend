package com.lumitest.recorder.application.service;

import com.lumitest.recorder.domain.model.SavedElement;
import com.lumitest.recorder.domain.port.out.ElementLibraryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElementLibraryService {

    private final ElementLibraryRepositoryPort repositoryPort;

    public void save(String domain, String label, String selector) {
        SavedElement element = SavedElement.builder()
                .domain(domain)
                .label(label)
                .selector(selector)
                .lastUpdated(LocalDateTime.now())
                .build();
        repositoryPort.save(element);
    }

    public String getSelector(String domain, String label) {
        // Simple implementation for now
        return repositoryPort.findAll().stream()
                .filter(e -> e.getDomain().equals(domain) && e.getLabel().equals(label))
                .map(SavedElement::getSelector)
                .findFirst()
                .orElse(null);
    }

    public List<SavedElement> getAll() {
        return repositoryPort.findAll();
    }

    public void delete(String id) {
        repositoryPort.deleteById(id);
    }
}
