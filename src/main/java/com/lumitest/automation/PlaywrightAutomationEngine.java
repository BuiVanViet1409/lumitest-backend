package com.lumitest.automation;

import com.microsoft.playwright.*;
import com.lumitest.entity.TestStep;
import com.lumitest.entity.StepResult;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.UUID;

@Service
public class PlaywrightAutomationEngine {

    public StepResult executeStep(Page page, TestStep step, String screenshotDir) {
        StepResult result = new StepResult();
        result.setStep(step);

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

        try {
            String screenshotName = UUID.randomUUID().toString() + ".png";
            java.nio.file.Files.createDirectories(Paths.get(screenshotDir));
            String fullPath = Paths.get(screenshotDir, screenshotName).toString();
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fullPath)));
            result.setScreenshotPath(screenshotName);
        } catch (Exception e) {
            // Log screenshot error
        }

        return result;
    }
}
