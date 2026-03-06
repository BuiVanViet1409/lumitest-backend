package com.lumitest.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepResult {
    private int stepOrder;
    private String status;
    private String errorMessage;
    private String screenshotPath;
}
