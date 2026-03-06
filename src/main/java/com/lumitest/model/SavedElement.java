package com.lumitest.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "element_library")
public class SavedElement {
    @Id
    private String id;
    private String domain; // e.g., google.com
    private String label; // e.g., "Login", "Search button"
    private String selector;
    private LocalDateTime lastUpdated;
}
