package com.lumitest.util;

import java.util.UUID;

public class PathUtils {

    public static String getExecutionDir(String basePath, String executionId) {
        return basePath + "/" + executionId;
    }

    public static String generateScreenshotName(int stepOrder) {
        return "step-" + stepOrder + "-" + UUID.randomUUID().toString().substring(0, 8) + ".png";
    }

    public static String getVideoName(com.microsoft.playwright.Video video) {
        if (video == null)
            return null;
        return video.path().getFileName().toString();
    }
}
