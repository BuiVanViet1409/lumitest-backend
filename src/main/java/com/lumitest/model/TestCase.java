package com.lumitest.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_cases")
public class TestCase {
    @Id
    private String id;
    private String name; // Title
    private String description;
    private String preconditions;
    private String scenario;
    private String folder;
    private String acceptanceCriteriaMapping;
    private LocalDateTime createdAt = LocalDateTime.now();

    @org.springframework.data.annotation.Transient
    private long stepsCount;
}
