package com.lumitest.model;

import lombok.Data;
import java.util.Map;

@Data
public class RecorderEvent {
    private String action; // CLICK, INPUT, NAVIGATE
    private String tagName;
    private String text;
    private String value;
    private String href;
    private String selector;
    private Map<String, String> attributes;
}
