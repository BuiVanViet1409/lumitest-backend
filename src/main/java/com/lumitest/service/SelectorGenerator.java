package com.lumitest.service;

import com.lumitest.model.RecorderEvent;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class SelectorGenerator {

    public String generate(RecorderEvent event) {
        Map<String, String> attrs = event.getAttributes();
        if (attrs == null)
            return "body";

        // 1. Priority: data-testid
        if (attrs.containsKey("data-testid")) {
            return "[data-testid='" + attrs.get("data-testid") + "']";
        }

        // 2. ID
        if (attrs.containsKey("id") && !attrs.get("id").isEmpty()) {
            return "#" + attrs.get("id");
        }

        // 3. Name (for inputs)
        if (attrs.containsKey("name") && !attrs.get("name").isEmpty()) {
            return event.getTagName() + "[name='" + attrs.get("name") + "']";
        }

        // 4. Aria Label
        if (attrs.containsKey("aria-label") && !attrs.get("aria-label").isEmpty()) {
            return "[aria-label='" + attrs.get("aria-label") + "']";
        }

        // 5. Placeholder
        if (attrs.containsKey("placeholder") && !attrs.get("placeholder").isEmpty()) {
            return "[placeholder='" + attrs.get("placeholder") + "']";
        }

        // 6. Button text / Link text logic
        if ("button".equalsIgnoreCase(event.getTagName()) && event.getText() != null && !event.getText().isEmpty()) {
            return "button:has-text('" + event.getText() + "')";
        }

        // 7. Fallback: CSS Path (very simple version for now)
        return event.getTagName() + (attrs.containsKey("class") ? "." + attrs.get("class").split(" ")[0] : "");
    }
}
