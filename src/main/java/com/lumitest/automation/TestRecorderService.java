package com.lumitest.automation;

import com.lumitest.model.TestStep;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TestRecorderService {

    public List<TestStep> recordSteps(String url) {
        List<TestStep> recordedSteps = new ArrayList<>();
        AtomicInteger order = new AtomicInteger(1);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            // Bước đầu tiên luôn là OPEN_URL
            TestStep openStep = new TestStep();
            openStep.setOrder(order.getAndIncrement());
            openStep.setDescription("Navigate to URL");
            openStep.setTestData(url);
            openStep.setVerificationType("UI");
            recordedSteps.add(openStep);

            page.navigate(url);

            // Lắng nghe các sự kiện cơ bản (Click và Input)
            page.exposeFunction("recordEvent", (args) -> {
                String type = (String) args[0];
                String selector = (String) args[1];
                String value = args.length > 2 ? (String) args[2] : null;

                String action = type.equals("click") ? "CLICK" : "INPUT_TEXT";

                TestStep step = new TestStep();
                step.setOrder(order.getAndIncrement());
                step.setDescription(action + " on " + selector);
                step.setTestData(value);
                step.setExpectedResult("Check " + action);
                step.setVerificationType("UI");
                recordedSteps.add(step);
                return null;
            });

            page.addInitScript("() => {" +
                    "  document.addEventListener('click', e => {" +
                    "    const selector = e.target.id ? '#' + e.target.id : e.target.tagName.toLowerCase();" +
                    "    window.recordEvent(['click', selector]);" +
                    "  }, true);" +
                    "  document.addEventListener('input', e => {" +
                    "    const selector = e.target.id ? '#' + e.target.id : e.target.tagName.toLowerCase();" +
                    "    window.recordEvent(['input', selector, e.target.value]);" +
                    "  }, true);" +
                    "}");

            // Đợi người dùng đóng trình duyệt để kết thúc ghi hình
            while (!browser.contexts().isEmpty() && !browser.contexts().get(0).pages().isEmpty()) {
                Thread.sleep(1000);
            }

            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recordedSteps;
    }
}
