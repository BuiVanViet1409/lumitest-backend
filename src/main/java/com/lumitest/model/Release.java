package com.lumitest.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "releases")
public class Release {
    @Id
    private String id;
    private String name;
    private String description;
    private String status; // OPEN / LOCKED / CLOSED
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Audit
    private String createdBy;
}
