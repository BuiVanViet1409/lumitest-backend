package com.lumitest.assistant.infrastructure.adapter.in.web;

import com.lumitest.assistant.application.service.QAAssistantService;
import com.lumitest.assistant.domain.model.QAAnalysisSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class AssistantWebAdapter {

    private final QAAssistantService qaAssistantService;

    @PostMapping("/generate")
    public QAResponse generate(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        return (QAResponse) qaAssistantService.generateTestCases(description);
    }

    @GetMapping("/history")
    public List<QAAnalysisSession> getHistory() {
        return qaAssistantService.getAllSessions();
    }

    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable("id") String id) {
        qaAssistantService.deleteSession(id);
    }

    @DeleteMapping("/history")
    public void clearHistory() {
        qaAssistantService.clearAllSessions();
    }
}
