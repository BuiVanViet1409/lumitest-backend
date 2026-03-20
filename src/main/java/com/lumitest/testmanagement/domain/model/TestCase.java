package com.lumitest.testmanagement.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCase {
    private String id;
    private String name;
    private String description;
    private String preconditions;
    private String scenario;
    private String folder;
    private String acceptanceCriteriaMapping;
    private String externalRequirementId;
    private List<String> defectIds;
    private String riskLevel;
    private String priority;
    private String businessImpact;
    private String author;
    private String reviewer;
    private String approvalStatus;
    private String releaseId;
    private LocalDateTime createdAt;
    private long stepsCount;
}
