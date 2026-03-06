package com.lumitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_executions")
@Data
public class TestExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_case_id")
    private TestCase testCase;

    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL)
    private List<StepResult> stepResults;
}
