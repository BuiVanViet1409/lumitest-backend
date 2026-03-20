package com.lumitest.recorder.infrastructure.adapter.in.web;

import com.lumitest.recorder.application.service.RecorderService;
import com.lumitest.recorder.domain.model.RecordingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recorder")
@RequiredArgsConstructor
public class RecorderWebAdapter {

    private final RecorderService recorderService;

    @PostMapping("/start")
    public RecordingSession start(
            @RequestParam("url") String url,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "preconditions", required = false) String preconditions) {
        return recorderService.startRecording(url, name, description, preconditions);
    }

    @PostMapping("/stop")
    public RecordingSession stop(@RequestParam("sessionId") String sessionId) {
        return recorderService.stopRecording(sessionId);
    }

    @GetMapping("/session/{sessionId}")
    public RecordingSession getSession(@PathVariable("sessionId") String sessionId) {
        return recorderService.getSession(sessionId);
    }
}
