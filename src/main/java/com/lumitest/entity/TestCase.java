package com.lumitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_cases")
@Data
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String applicationUrl;
    private String username;
    private String password;
    private String createdBy;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "testCase", cascade = CascadeType.ALL)
    private List<TestStep> steps;
}
