package com.lumitest.service;

import com.lumitest.model.RecorderEvent;
import com.lumitest.model.TestStep;
import com.lumitest.constant.TestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StepBuilderService {

    @Autowired
    private SelectorGenerator selectorGenerator;

    public TestStep build(RecorderEvent event, int order) {
        TestStep step = new TestStep();
        step.setOrder(order);

        String action = mapAction(event.getAction());
        step.setAction(action);

        if (!TestConstants.Action.OPEN_URL.equals(action) && !TestConstants.Action.WAIT.equals(action)) {
            step.setSelector(selectorGenerator.generate(event));
        }

        if (TestConstants.Action.INPUT_TEXT.equals(action)) {
            step.setValue(event.getValue());
        } else if (TestConstants.Action.OPEN_URL.equals(action)) {
            step.setValue(event.getHref());
        }

        return step;
    }

    private String mapAction(String browserAction) {
        if (browserAction == null)
            return TestConstants.Action.WAIT;
        switch (browserAction.toUpperCase()) {
            case "CLICK":
                return TestConstants.Action.CLICK;
            case "INPUT":
            case "CHANGE":
                return TestConstants.Action.INPUT_TEXT;
            case "NAVIGATE":
                return TestConstants.Action.OPEN_URL;
            default:
                return TestConstants.Action.WAIT;
        }
    }
}
