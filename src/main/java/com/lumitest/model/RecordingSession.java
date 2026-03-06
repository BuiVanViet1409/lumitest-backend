package com.lumitest.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class RecordingSession {
    private String id;
    private String targetUrl;
    private List<TestStep> steps = new ArrayList<>();
    private boolean active;
}
