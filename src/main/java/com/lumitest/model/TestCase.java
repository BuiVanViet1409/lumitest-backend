package com.lumitest.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_cases")
public class TestCase {
    @Id
    private String id;
    private String name;
    private String description;
    private String applicationUrl;
    private String createdBy;
    private LocalDateTime createdAt = LocalDateTime.now();
    private List<TestStep> steps;
}
