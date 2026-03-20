package com.lumitest.recorder.domain.port.out;

import com.lumitest.recorder.domain.model.SavedElement;
import java.util.List;
import java.util.Optional;

public interface ElementLibraryRepositoryPort {
    SavedElement save(SavedElement element);
    Optional<SavedElement> findById(String id);
    List<SavedElement> findAll();
    void deleteById(String id);
}
