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
public class Execution {
    private String id;
    private String testCaseId;
    private String status; // PENDING / RUNNING / PASSED / FAILED
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String videoPath;
    private String progressMessage;
    private String environment;
    private String configProfile;
    private String testDataProfile;
}
