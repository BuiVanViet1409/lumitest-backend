package com.lumitest.assistant.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MongoQAAnalysisRepository extends MongoRepository<QAAnalysisSessionEntity, String> {
    List<QAAnalysisSessionEntity> findAllByOrderByCreatedAtDesc();
}
