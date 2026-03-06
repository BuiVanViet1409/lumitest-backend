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
    private TimeoutConfig timeouts = new TimeoutConfig();

    private int maxTestSteps = 50;

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
}
