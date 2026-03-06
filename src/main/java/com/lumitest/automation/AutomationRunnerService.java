package com.lumitest.automation;

import com.microsoft.playwright.*;
import com.lumitest.model.TestStep;
import com.lumitest.model.ExecutionStep;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import com.lumitest.constant.TestConstants;
import com.lumitest.util.HeuristicUtils;
import com.lumitest.util.PathUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AutomationRunnerService {

    @org.springframework.beans.factory.annotation.Autowired
    private com.lumitest.config.LumiTestConfig config;

    public ExecutionStep executeStep(Page page, TestStep step, String executionId) {
        ExecutionStep result = new ExecutionStep();
        result.setExecutionId(executionId);
        result.setStepOrder(step.getOrder());
        result.setAction(step.getAction());

        int attempts = 0;
        int maxRetries = step.getRetryCount();

        while (attempts <= maxRetries) {
            try {
                attempts++;
                switch (step.getAction().toUpperCase()) {
                    case TestConstants.Action.OPEN_URL:
                        page.navigate(step.getValue());
                        break;
                    case TestConstants.Action.INPUT_TEXT:
                        smartFill(page, step.getSelector(), step.getValue());
                        break;
                    case TestConstants.Action.CLICK:
                        smartClick(page, step.getSelector());
                        break;
                    case TestConstants.Action.WAIT:
                        try {
                            page.waitForTimeout(Double.parseDouble(step.getValue()));
                        } catch (Exception e) {
                            page.waitForTimeout(2000);
                        }
                        break;
                    case TestConstants.Action.ASSERT_TEXT:
                        // Chờ phần tử xuất hiện và chứa đúng text mới assert để screenshot bắt được
                        // đúng màn hình thành công
                        page.waitForCondition(() -> {
                            String content = page.textContent(step.getSelector());
                            return content != null && content.toLowerCase().contains(step.getValue().toLowerCase());
                        }, new Page.WaitForConditionOptions().setTimeout(config.getTimeouts().getFallback()));
                        break;
                    default:
                        throw new UnsupportedOperationException("Action not supported: " + step.getAction());
                }

                result.setStatus(TestConstants.Status.PASS);
                result.setErrorMessage(null);
                break;

            } catch (Exception e) {
                result.setStatus(TestConstants.Status.FAIL);
                result.setErrorMessage("Attempt " + attempts + ": " + e.getMessage());
                if (attempts <= maxRetries) {
                    page.waitForTimeout(1000);
                }
            }
        }

        // Capture screenshot after each step as proof
        try {
            // Chờ trang ổn định sâu (Network Idle) để screenshot bắt được trạng thái cuối
            // cùng sau khi chuyển trang
            try {
                // Ưu tiên đợi network rảnh tay (đặc biệt sau login/click)
                page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                        new Page.WaitForLoadStateOptions().setTimeout(2000));

                // Thêm 1 giây settle time cứng để bắt các hiệu ứng render cuối cùng
                page.waitForTimeout(1000);
            } catch (Exception e) {
                // Nếu network không idle kịp trong 2s thì cũng chụp ảnh luôn để không block
                // tiến trình
                log.debug("Network didn't reach idle state within 2s, proceeding with screenshot.");
            }

            String screenshotDir = PathUtils.getExecutionDir(config.getScreenshotPath(), executionId);
            String screenshotName = PathUtils.generateScreenshotName(step.getOrder());
            Files.createDirectories(Paths.get(screenshotDir));
            String fullPath = Paths.get(screenshotDir, screenshotName).toString();
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fullPath)));

            // Store relative path for frontend access
            result.setScreenshotPath(screenshotName);
        } catch (Exception e) {
            log.error("Failed to capture screenshot: {}", e.getMessage());
        }

        return result;
    }

    private void smartFill(Page page, String selector, String value) {
        // Priority 0: Nếu selector đã là "xịn" (ID hoặc data-testid từ Recorder), chạy
        // luôn
        if (HeuristicUtils.isHighPrioritySelector(selector)) {
            try {
                page.fill(selector, value, new Page.FillOptions().setTimeout(config.getTimeouts().getFallback()));
                return;
            } catch (Exception e) {
                /* Tiếp tục thử heuristic nếu selector xịn bị lỗi or element chưa kịp load */ }
        }

        String[] targets = { selector };
        if (HeuristicUtils.isEmailField(selector))
            targets = HeuristicUtils.getEmailKeywords(selector);
        else if (HeuristicUtils.isPasswordField(selector))
            targets = HeuristicUtils.getPasswordKeywords(selector);
        else if (HeuristicUtils.isPhoneField(selector))
            targets = HeuristicUtils.getPhoneKeywords(selector);
        else if (HeuristicUtils.isUsernameField(selector))
            targets = HeuristicUtils.getUsernameKeywords(selector);

        log.info("  🧠 Analyzing {} field (Keywords: {})", selector, String.join(", ", targets));

        for (String target : targets) {
            try {
                // Thử Placeholder & Label & Name gần như cùng lúc (fail nhanh)
                try {
                    log.info("  🔍 Trying placeholder: {}", target);
                    page.getByPlaceholder(target, new Page.GetByPlaceholderOptions().setExact(false))
                            .fill(value, new Locator.FillOptions().setTimeout(config.getTimeouts().getHeuristic()));
                    return;
                } catch (Exception e) {
                }

                try {
                    log.info("  🔍 Trying label: {}", target);
                    page.getByLabel(target, new Page.GetByLabelOptions().setExact(false))
                            .fill(value, new Locator.FillOptions().setTimeout(config.getTimeouts().getHeuristic()));
                    return;
                } catch (Exception e) {
                }

                try {
                    log.info("  🔍 Trying name attribute: [name*='{}']", target);
                    page.locator("input[name*='" + target + "'], textarea[name*='" + target + "']").first()
                            .fill(value, new Locator.FillOptions().setTimeout(config.getTimeouts().getHeuristic()));
                    return;
                } catch (Exception e) {
                }

                try {
                    log.info("  🔍 Trying title: {}", target);
                    page.getByTitle(target, new Page.GetByTitleOptions().setExact(false))
                            .fill(value, new Locator.FillOptions().setTimeout(config.getTimeouts().getHeuristic()));
                    return;
                } catch (Exception e) {
                }

                try {
                    log.info("  🔍 Trying proximity near text: {}", target);
                    // Chỉ tìm các input/textarea ngay sau đoạn text, không quét toàn bộ DOM
                    page.locator("text=" + target)
                            .locator("xpath=./following::input[1]|./following::textarea[1]|..//input|..//textarea")
                            .first()
                            .fill(value, new Locator.FillOptions().setTimeout(config.getTimeouts().getHeuristic()));
                    return;
                } catch (Exception e) {
                }

            } catch (Exception e) {
                /* Chuyển sang keyword tiếp theo */ }
        }
        // Fallback to CSS
        if (HeuristicUtils.isCssSelector(selector)) {
            log.info("  🔍 Falling back to CSS: {}", selector);
            page.fill(selector, value, new Page.FillOptions().setTimeout(config.getTimeouts().getFallback()));
        } else {
            // Last ditch: Tìm ô input đầu tiên có vẻ liên quan nhất
            log.warn("  🔍 Final attempt: Searching for first available input since '{}' failed", selector);
            try {
                page.locator("input, textarea").first()
                        .fill(value, new Locator.FillOptions().setTimeout(config.getTimeouts().getFallback()));
            } catch (Exception e) {
                log.error("  ❌ Failure: No input element found for {}", selector);
            }
        }
    }

    private void smartClick(Page page, String selector) {
        boolean isLogin = HeuristicUtils.isLoginAction(selector);

        if (HeuristicUtils.isHighPrioritySelector(selector)) {
            try {
                page.click(selector, new Page.ClickOptions().setTimeout(config.getTimeouts().getFallback()));
                if (isLogin) {
                    // Ép trình duyệt phải DOMCONTENTLOADED (bắt đầu tải trang mới) sau khi nhấn
                    // Login
                    try {
                        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                                new Page.WaitForLoadStateOptions().setTimeout(2000));
                    } catch (Exception e) {
                    }
                }
                return;
            } catch (Exception e) {
            }
        }

        String[] targets = { selector };
        if (isLogin)
            targets = HeuristicUtils.getLoginKeywords(selector);

        for (String target : targets) {
            try {
                // 1. Tìm Button theo Name/Text
                Locator btn = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName(target).setExact(false));
                btn.click(new Locator.ClickOptions().setTimeout(config.getTimeouts().getHeuristic()));

                if (isLogin) {
                    try {
                        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                                new Page.WaitForLoadStateOptions().setTimeout(2000));
                    } catch (Exception e) {
                    }
                }
                return;
            } catch (Exception e1) {
                try {
                    // 2. Tìm Link theo Name/Text
                    Locator link = page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                            new Page.GetByRoleOptions().setName(target).setExact(false));
                    link.click(new Locator.ClickOptions().setTimeout(config.getTimeouts().getHeuristic()));

                    if (isLogin) {
                        try {
                            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                                    new Page.WaitForLoadStateOptions().setTimeout(2000));
                        } catch (Exception e) {
                        }
                    }
                    return;
                } catch (Exception e2) {
                    try {
                        // 3. Tìm theo Text thuần túy
                        Locator text = page.getByText(target, new Page.GetByTextOptions().setExact(false));
                        text.click(new Locator.ClickOptions().setTimeout(config.getTimeouts().getHeuristic()));

                        if (isLogin) {
                            try {
                                page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                                        new Page.WaitForLoadStateOptions().setTimeout(2000));
                            } catch (Exception e) {
                            }
                        }
                        return;
                    } catch (Exception e3) {
                        try {
                            // 4. Tìm theo thuộc tính name
                            page.locator("[name*='" + target + "']").first()
                                    .click(new Locator.ClickOptions().setTimeout(config.getTimeouts().getHeuristic()));
                            return;
                        } catch (Exception e4) {
                            /* Continue */ }
                    }
                }
            }
        }

        if (HeuristicUtils.isCssSelector(selector)) {
            page.click(selector, new Page.ClickOptions().setTimeout(config.getTimeouts().getFallback()));
        } else {
            page.getByText(selector, new Page.GetByTextOptions().setExact(false))
                    .click(new Locator.ClickOptions().setTimeout(config.getTimeouts().getFallback()));
        }
    }
}
