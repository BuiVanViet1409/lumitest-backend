package com.lumitest.testmanagement.infrastructure.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_cases")
public class TestCaseEntity {
    @Id
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
}
