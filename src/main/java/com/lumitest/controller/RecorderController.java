package com.lumitest.controller;

import com.lumitest.model.RecordingSession;
import com.lumitest.service.RecorderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/recorder")
@CrossOrigin("*")
@RequiredArgsConstructor
public class RecorderController {

    private final RecorderService recorderService;

    @PostMapping("/start")
    public RecordingSession start(
            @RequestParam("url") String url,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "preconditions", required = false) String preconditions) {
        return recorderService.start(url, name, description, preconditions);
    }

    @PostMapping("/stop")
    public RecordingSession stop(@RequestParam("sessionId") String sessionId) {
        return recorderService.stop(sessionId);
    }

    @GetMapping("/session/{sessionId}")
    public RecordingSession getSession(@PathVariable("sessionId") String sessionId) {
        return recorderService.getSession(sessionId);
    }

    @PostMapping("/reset")
    public void reset() {
        recorderService.resetSession();
    }
}
