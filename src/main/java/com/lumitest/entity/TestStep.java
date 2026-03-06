package com.lumitest.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "test_steps")
@Data
public class TestStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_case_id")
    private TestCase testCase;

    private Integer stepOrder;
    private String action; // Action Enum string
    private String selector;
    private String value;
    private String expectedResult;
}
