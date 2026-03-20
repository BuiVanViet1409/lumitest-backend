package com.lumitest.assistant.infrastructure.adapter.out.persistence;

import com.lumitest.assistant.domain.model.QAAnalysisSession;
import com.lumitest.assistant.domain.port.out.QAAnalysisRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QAAnalysisPersistenceAdapter implements QAAnalysisRepositoryPort {

    private final MongoQAAnalysisRepository repository;

    @Override
    public QAAnalysisSession save(QAAnalysisSession session) {
        return mapToDomain(repository.save(mapToEntity(session)));
    }

    @Override
    public Optional<QAAnalysisSession> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<QAAnalysisSession> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    private QAAnalysisSessionEntity mapToEntity(QAAnalysisSession domain) {
        return QAAnalysisSessionEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .question(domain.getQuestion())
                .answer(domain.getAnswer())
                .context(domain.getContext())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private QAAnalysisSession mapToDomain(QAAnalysisSessionEntity entity) {
        return QAAnalysisSession.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .question(entity.getQuestion())
                .answer(entity.getAnswer())
                .context(entity.getContext())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
