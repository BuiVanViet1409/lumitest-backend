package com.lumitest.recorder.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecorderEvent {
    private String action;
    private String tagName;
    private String text;
    private String value;
    private String href;
    private String selector;
    private Map<String, String> attributes;
}
