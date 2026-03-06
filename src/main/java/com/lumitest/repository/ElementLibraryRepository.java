package com.lumitest.repository;

import com.lumitest.model.SavedElement;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ElementLibraryRepository extends MongoRepository<SavedElement, String> {
    Optional<SavedElement> findByDomainAndLabel(String domain, String label);
}
