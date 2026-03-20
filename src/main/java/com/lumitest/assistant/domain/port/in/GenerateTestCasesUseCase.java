package com.lumitest.assistant.domain.port.in;

import com.lumitest.assistant.domain.model.QAAnalysisSession;
import java.util.List;

public interface GenerateTestCasesUseCase {
    Object generateTestCases(String ticketDescription); // Returning Object for now to avoid DTO dependency in domain
    List<QAAnalysisSession> getAllSessions();
    void deleteSession(String id);
    void clearAllSessions();
    List<String> suggestFields(String intent);
}
