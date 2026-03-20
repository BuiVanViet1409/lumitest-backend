package com.lumitest.service;

import com.lumitest.model.Release;
import com.lumitest.repository.ReleaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReleaseService {
    private final ReleaseRepository releaseRepository;

    public List<Release> getAllReleases() {
        return releaseRepository.findAll();
    }

    public Release getReleaseById(String id) {
        return releaseRepository.findById(id).orElse(null);
    }

    public Release saveRelease(Release release) {
        log.info("Saving release: {}", release.getName());
        return releaseRepository.save(release);
    }

    public void deleteRelease(String id) {
        log.info("Deleting release: {}", id);
        releaseRepository.deleteById(id);
    }
}
