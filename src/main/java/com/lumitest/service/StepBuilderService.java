package com.lumitest.service;

import com.lumitest.model.RecorderEvent;
import com.lumitest.model.TestStep;
import org.springframework.stereotype.Service;

@Service
public class StepBuilderService {

    public TestStep build(RecorderEvent event, int order) {
        TestStep step = new TestStep();
        step.setOrder(order);
        step.setDescription(event.getAction() + " on " + event.getSelector());
        step.setTestData(event.getValue() != null ? event.getValue() : event.getHref());
        step.setExpectedResult("System should process " + event.getAction());
        step.setVerificationType("UI");
        return step;
    }
}
