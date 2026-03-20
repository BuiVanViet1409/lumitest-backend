package com.lumitest.recorder.domain.port.in;

import com.lumitest.recorder.domain.model.RecordingSession;
import com.lumitest.recorder.domain.model.RecorderEvent;

public interface ManageRecordingUseCase {
    RecordingSession startRecording(String targetUrl, String name, String description, String preconditions);
    void addEvent(String sessionId, RecorderEvent event);
    RecordingSession stopRecording(String sessionId);
    RecordingSession getSession(String sessionId);
}
