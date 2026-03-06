package com.lumitest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "execution_steps")
public class ExecutionStep {
    @Id
    private String id;
    private String executionId;
    private int stepOrder;
    private String action;
    private String status; // PASS / FAIL
    private String screenshotPath;
    private String errorMessage;
}
