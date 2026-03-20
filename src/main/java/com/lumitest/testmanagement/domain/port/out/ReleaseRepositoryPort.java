package com.lumitest.testmanagement.domain.port.out;

import com.lumitest.testmanagement.domain.model.Release;
import java.util.List;
import java.util.Optional;

public interface ReleaseRepositoryPort {
    Release save(Release release);
    Optional<Release> findById(String id);
    List<Release> findAll();
    void deleteById(String id);
}
