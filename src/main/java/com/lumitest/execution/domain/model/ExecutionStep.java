package com.lumitest.execution.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionStep {
    private String id;
    private String executionId;
    private String testStepId;
    private int stepOrder;
    private String action;
    private String status; // PASSED / FAILED
    private String screenshotPath;
    private String errorMessage;
    private long duration;
    private LocalDateTime executedAt;
}
