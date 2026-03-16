package com.lumitest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper for the AI QA Assistant response containing multiple test cases.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QAResponse {
    private String ticketTitle;
    private List<QATestCase> testCases;
}
