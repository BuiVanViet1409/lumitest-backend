package com.lumitest.assistant.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QAAnalysisSession {
    private String id;
    private String title;
    private String question;
    private String answer;
    private String context;
    private LocalDateTime createdAt;
}
