package com.lumitest.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestStep {
    private int order;
    private String action;
    private String selector;
    private String value;
    private int retryCount = 0;
}
