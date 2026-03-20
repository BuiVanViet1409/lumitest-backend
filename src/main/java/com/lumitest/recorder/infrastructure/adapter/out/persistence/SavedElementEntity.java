package com.lumitest.recorder.infrastructure.adapter.out.persistence;

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
@Document(collection = "element_library")
public class SavedElementEntity {
    @Id
    private String id;
    private String domain;
    private String label;
    private String selector;
    private LocalDateTime lastUpdated;
}
