package com.lumitest.automation;

import com.microsoft.playwright.*;
import com.lumitest.model.TestStep;
import com.lumitest.model.StepResult;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class PlaywrightAutomationEngine {

    public StepResult executeStep(Page page, TestStep step, String executionId) {
        StepResult result = new StepResult();
        result.setStepOrder(step.getOrder());

        try {
            switch (step.getAction().toUpperCase()) {
                case "OPEN_URL":
                    page.navigate(step.getValue());
                    break;
                case "INPUT_TEXT":
                    page.fill(step.getSelector(), step.getValue());
                    break;
                case "CLICK":
                    page.click(step.getSelector());
                    break;
                case "WAIT":
                    page.waitForTimeout(Double.parseDouble(step.getValue()));
                    break;
                case "ASSERT_VISIBLE":
                    if (!page.isVisible(step.getSelector())) {
                        throw new RuntimeException("Element not visible: " + step.getSelector());
                    }
                    break;
                case "ASSERT_TEXT":
                    String actualText = page.textContent(step.getSelector());
                    if (actualText == null || !actualText.contains(step.getValue())) {
                        throw new RuntimeException(
                                "Expected text '" + step.getValue() + "' not found in '" + actualText + "'");
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Action not supported: " + step.getAction());
            }

            result.setStatus("PASS");

        } catch (Exception e) {
            result.setStatus("FAIL");
            result.setErrorMessage(e.getMessage());
        }

        // Capture screenshot
        try {
            String screenshotDir = "screenshots/" + executionId;
            String screenshotName = "step-" + step.getOrder() + "-" + UUID.randomUUID().toString().substring(0, 8)
                    + ".png";
            Files.createDirectories(Paths.get(screenshotDir));
            String fullPath = Paths.get(screenshotDir, screenshotName).toString();
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fullPath)));
            result.setScreenshotPath(fullPath);
        } catch (Exception e) {
            // Silently fail screenshot capture
        }

        return result;
    }
}
