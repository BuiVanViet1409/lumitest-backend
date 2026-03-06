package com.lumitest.service;

import com.lumitest.model.SavedElement;
import com.lumitest.repository.ElementLibraryRepository;
import com.lumitest.util.HeuristicUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ElementLibraryService {

    @Autowired
    private ElementLibraryRepository repository;

    public void saveElement(String url, String label, String selector) {
        if (label == null || label.isEmpty() || selector == null || selector.isEmpty())
            return;

        String domain = HeuristicUtils.extractDomain(url);
        Optional<SavedElement> existing = repository.findByDomainAndLabel(domain, label);

        SavedElement element = existing.orElse(new SavedElement());
        element.setDomain(domain);
        element.setLabel(label);
        element.setSelector(selector);
        element.setLastUpdated(LocalDateTime.now());

        repository.save(element);
    }

    public String getSelector(String url, String label) {
        String domain = HeuristicUtils.extractDomain(url);
        return repository.findByDomainAndLabel(domain, label)
                .map(SavedElement::getSelector)
                .orElse(null);
    }
}
