package com.lumitest.service;

import com.lumitest.model.RecorderEvent;
import com.lumitest.model.TestStep;
import org.springframework.stereotype.Service;

@Service
public class StepBuilderService {

    public TestStep build(RecorderEvent event, int order) {
        TestStep step = new TestStep();
        step.setOrder(order);
        step.setAction(event.getAction()); // Action is already in our expected format from JS

        // Selector is computed smartly by recorder.js
        if (event.getSelector() != null) {
            step.setSelector(event.getSelector());
        }

        if (event.getValue() != null) {
            step.setValue(event.getValue());
        } else if (event.getHref() != null) {
            step.setValue(event.getHref());
        }

        return step;
    }
}
