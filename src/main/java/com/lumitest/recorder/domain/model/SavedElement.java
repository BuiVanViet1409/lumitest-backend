package com.lumitest.recorder.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedElement {
    private String id;
    private String domain;
    private String label;
    private String selector;
    private LocalDateTime lastUpdated;
}
