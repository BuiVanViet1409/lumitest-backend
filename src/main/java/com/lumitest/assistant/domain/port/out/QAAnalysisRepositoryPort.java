package com.lumitest.assistant.domain.port.out;

import com.lumitest.assistant.domain.model.QAAnalysisSession;
import java.util.List;
import java.util.Optional;

public interface QAAnalysisRepositoryPort {
    QAAnalysisSession save(QAAnalysisSession session);
    Optional<QAAnalysisSession> findById(String id);
    List<QAAnalysisSession> findAll();
    void deleteById(String id);
}
