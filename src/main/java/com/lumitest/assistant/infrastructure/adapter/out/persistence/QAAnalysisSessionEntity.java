package com.lumitest.assistant.infrastructure.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "qa_analysis_sessions")
public class QAAnalysisSessionEntity {
    @Id
    private String id;
    private String title;
    private String question;
    private String answer;
    private String context;
    private LocalDateTime createdAt;
}
