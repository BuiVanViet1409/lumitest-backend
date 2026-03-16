package com.lumitest.repository;

import com.lumitest.model.QAAnalysisSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

/**
 * Repository for AI QA analysis sessions.
 */
public interface QAAnalysisSessionRepository extends MongoRepository<QAAnalysisSession, String> {
    List<QAAnalysisSession> findAllByOrderByCreatedAtDesc();
}
