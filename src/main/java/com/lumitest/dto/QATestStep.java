package com.lumitest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QATestStep {
    private String description;
    private String testData;
    private String expectedResult;
    private String verificationType;
    private String verificationRule;
}
