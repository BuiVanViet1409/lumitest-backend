package com.lumitest.model;

import com.lumitest.dto.QAResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Model to persist AI QA analysis results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "qa_sessions")
public class QAAnalysisSession {
    @Id
    private String id;
    private String ticketDescription;
    private LocalDateTime createdAt;
    private QAResponse result;
}
