package com.lumitest.testmanagement.application.service;

import com.lumitest.recorder.domain.model.RecorderEvent;
import com.lumitest.testmanagement.domain.model.TestStep;
import org.springframework.stereotype.Service;

@Service
public class StepBuilderService {

    public TestStep build(RecorderEvent event, int order) {
        return TestStep.builder()
                .order(order)
                .description(event.getAction() + " on " + event.getSelector())
                .testData(event.getValue() != null ? event.getValue() : event.getHref())
                .expectedResult("System should process " + event.getAction())
                .verificationType("UI")
                .build();
    }
}
