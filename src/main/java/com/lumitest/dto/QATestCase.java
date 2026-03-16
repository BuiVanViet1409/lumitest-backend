package com.lumitest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing a structured test case generated from a
 * ticket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QATestCase {
    private String title;
    private String description;
    private String preconditions;
    private String acceptanceCriteriaMapping;
    private List<QATestStep> steps;
}
