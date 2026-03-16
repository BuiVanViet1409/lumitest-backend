package com.lumitest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "lumitest")
public class LumiTestConfig {

    private String screenshotPath = "src/main/resources/static/screenshots";

    private BrowserConfig browser = new BrowserConfig();
    private RecorderConfig recorder = new RecorderConfig();
    private TimeoutConfig timeouts = new TimeoutConfig();
    private AIConfig ai = new AIConfig();

    private int maxTestSteps = 50;

    @Data
    public static class RecorderConfig {
        /** Trình duyệt sử dụng (msedge, chrome) */
        private String browser = "msedge";

        /** Thư mục lưu trữ session (cookies) */
        private String sessionPath = "automation/sessions/qc-session";

        /** Độ trễ giữa các hành động (ms) */
        private int delayMs = 50;

        /** Chế độ ẩn trình duyệt */
        private boolean headless = false;
    }

    @Data
    public static class BrowserConfig {
        private String type = "chromium";
        private boolean headless = true;
    }

    @Data
    public static class TimeoutConfig {
        private int global = 10000;
        private int heuristic = 300; // Giảm xuống để fail fast
        private int fallback = 1000; // Giảm xuống 1s
    }

    @Data
    public static class AIConfig {
        /** Google Gemini API Key */
        private String apiKey;
        /** Gemini Model name (e.g., gemini-flash-latest) */
        private String model = "gemini-flash-latest";
    }
}
