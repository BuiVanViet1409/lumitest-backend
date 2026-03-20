package com.lumitest.assistant.infrastructure.adapter.in.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

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
