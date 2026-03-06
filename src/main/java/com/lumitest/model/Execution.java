package com.lumitest.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "executions")
public class Execution {
    @Id
    private String id;
    private String testCaseId;
    private String status; // PENDING / RUNNING / PASSED / FAILED
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String videoPath; // Đường dẫn đến file video .webm
    private List<StepResult> stepResults;
}
