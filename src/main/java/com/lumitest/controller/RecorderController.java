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
    public RecordingSession start(@RequestParam("url") String url) {
        return recorderService.start(url);
    }

    @PostMapping("/stop")
    public RecordingSession stop(@RequestParam("sessionId") String sessionId, @RequestParam("name") String name) {
        return recorderService.stop(sessionId, name);
    }

    @GetMapping("/session/{sessionId}")
    public RecordingSession getSession(@PathVariable("sessionId") String sessionId) {
        return recorderService.getSession(sessionId);
    }
}
