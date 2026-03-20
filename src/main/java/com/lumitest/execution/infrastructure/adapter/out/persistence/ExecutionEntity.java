package com.lumitest.execution.infrastructure.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "executions")
public class ExecutionEntity {
    @Id
    private String id;
    private String testCaseId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String videoPath;
    private String progressMessage;
    private String environment;
    private String configProfile;
    private String testDataProfile;
}
