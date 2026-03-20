package com.lumitest.controller;

import com.lumitest.model.Release;
import com.lumitest.service.ReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
@Slf4j
public class ReleaseController {
    private final ReleaseService releaseService;

    @GetMapping
    public List<Release> getAll() {
        return releaseService.getAllReleases();
    }

    @PostMapping
    public Release create(@RequestBody Release release) {
        return releaseService.saveRelease(release);
    }

    @GetMapping("/{id}")
    public Release get(@PathVariable String id) {
        return releaseService.getReleaseById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        releaseService.deleteRelease(id);
    }
}
