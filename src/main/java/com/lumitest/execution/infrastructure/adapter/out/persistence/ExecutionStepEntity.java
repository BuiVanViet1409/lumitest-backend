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
@Document(collection = "execution_steps")
public class ExecutionStepEntity {
    @Id
    private String id;
    private String executionId;
    private String testStepId;
    private int stepOrder;
    private String action;
    private String status;
    private String screenshotPath;
    private String errorMessage;
    private long duration;
    private LocalDateTime executedAt;
}
