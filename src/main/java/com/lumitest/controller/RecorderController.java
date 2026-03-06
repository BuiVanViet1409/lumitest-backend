package com.lumitest.controller;

import com.lumitest.model.RecordingSession;
import com.lumitest.service.RecorderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recorder")
@CrossOrigin("*")
public class RecorderController {

    @Autowired
    private RecorderService recorderService;

    @PostMapping("/start")
    public RecordingSession start(@RequestParam String url) {
        return recorderService.start(url);
    }

    @PostMapping("/stop")
    public RecordingSession stop(@RequestParam String sessionId, @RequestParam String name) {
        return recorderService.stop(sessionId, name);
    }

    @GetMapping("/session/{sessionId}")
    public RecordingSession getSession(@PathVariable String sessionId) {
        return recorderService.getSession(sessionId);
    }
}
