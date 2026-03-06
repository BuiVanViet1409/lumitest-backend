package com.lumitest.automation;

import com.lumitest.model.TestStep;
import com.microsoft.playwright.*;
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
            recordedSteps.add(new TestStep(order.getAndIncrement(), "OPEN_URL", null, url, 0));

            page.navigate(url);

            // Lắng nghe các sự kiện cơ bản (Click và Input)
            // Lưu ý: Đây là phiên bản đơn giản hóa. Thực tế sẽ cần JS injection phức tạp
            // hơn.
            page.exposeFunction("recordEvent", (args) -> {
                String type = (String) args[0];
                String selector = (String) args[1];
                String value = args.length > 2 ? (String) args[2] : null;

                String action = type.equals("click") ? "CLICK" : "INPUT_TEXT";
                recordedSteps.add(new TestStep(order.getAndIncrement(), action, selector, value, 0));
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
