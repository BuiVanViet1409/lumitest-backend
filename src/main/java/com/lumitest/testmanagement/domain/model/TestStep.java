package com.lumitest.testmanagement.domain.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStep {
    private String id;
    private String testCaseId;
    private int order;
    private String description;
    private String testData;
    private String expectedResult;
    private String verificationType;
    private String verificationRule;
    private String status;
    private int retryCount;
}
