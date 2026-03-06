package com.lumitest.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "step_results")
@Data
public class StepResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "execution_id")
    private TestExecution execution;

    @ManyToOne
    @JoinColumn(name = "step_id")
    private TestStep step;

    private String status;
    private String screenshotPath;
    private String errorMessage;
}
