package com.lumitest.datainspection.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumitest.assistant.domain.port.in.GenerateTestCasesUseCase;
import com.lumitest.datainspection.application.service.DataDiscoveryService;
import com.lumitest.datainspection.domain.model.ComparisonResult;
import com.lumitest.util.discovery.DiscoveryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DataInspectionWebAdapter {

    private final DataDiscoveryService discoveryService;
    private final GenerateTestCasesUseCase assistantUseCase;

    @GetMapping("/discovery")
    public Map<String, Optional<DiscoveryResult>> discover(@RequestParam String fieldName) {
        return discoveryService.discoverAcrossAll(fieldName);
    }

    @PostMapping("/inspect/connect")
    public Map<String, Boolean> validate(@RequestBody ConnectionParams params) {
        return Map.of("success", true);
    }

    @PostMapping("/inspect/search")
    public List<DiscoveryResult> inspect(@RequestBody ConnectionParams params) {
        Map<String, Optional<DiscoveryResult>> results = discoveryService.discoverAcrossAll(params.getFieldName());
        return results.values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @PostMapping("/inspect/compare")
    public ComparisonResult compare(@RequestBody Map<String, Object> request) {
        ObjectMapper mapper = new ObjectMapper();
        ConnectionParams sourceA = mapper.convertValue(request.get("sourceA"), ConnectionParams.class);
        ConnectionParams sourceB = mapper.convertValue(request.get("sourceB"), ConnectionParams.class);
        String fieldName = (String) request.get("fieldName");
        
        return discoveryService.compareFields(sourceA, sourceB, fieldName);
    }

    @PostMapping("/inspect/suggest")
    public List<String> suggest(@RequestBody Map<String, String> request) {
        return assistantUseCase.suggestFields(request.get("intent"));
    }

    @GetMapping("/discovery/schema")
    public Map<String, List<String>> getSchema() {
        return discoveryService.getSchema();
    }
}
