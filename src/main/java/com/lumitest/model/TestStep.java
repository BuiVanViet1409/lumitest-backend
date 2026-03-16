package com.lumitest.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_steps")
public class TestStep {
    @Id
    private String id;
    private String testCaseId;
    private int order;
    private String description;
    private String testData;
    private String expectedResult;
    private String verificationType; // UI, API, DATABASE, MESSAGE
    private String verificationRule; // CONFIG
    private String status = "NOT_RUN";
    private int retryCount = 0;
}
