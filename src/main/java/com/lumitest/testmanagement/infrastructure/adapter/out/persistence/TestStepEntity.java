package com.lumitest.testmanagement.infrastructure.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_steps")
public class TestStepEntity {
    @Id
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
