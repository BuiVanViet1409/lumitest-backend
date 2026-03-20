package com.lumitest.recorder.domain.model;

import com.lumitest.testmanagement.domain.model.TestStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingSession {
    private String id;
    private String targetUrl;
    private String name;
    private String description;
    private String preconditions;
    private List<TestStep> steps;
    private boolean active;
}
