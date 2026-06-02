package com.framework.utils;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Screenshot capture utility. Saves a full-page PNG to disk and returns
 * the raw bytes so callers can also attach them to Allure without a
 * second capture.
 */
@Slf4j
public final class ScreenshotUtils {

    private static final String SCREENSHOTS_DIR = "target/screenshots/";

    private ScreenshotUtils() {}

    /**
     * Captures a full-page screenshot, writes it to disk, and returns the bytes.
     *
     * @return PNG bytes, or null if capture failed.
     */
    public static byte[] capture(Page page, String testName) {
        if (page == null) return null;
        try {
            Files.createDirectories(Paths.get(SCREENSHOTS_DIR));
            Path path = Paths.get(SCREENSHOTS_DIR + testName + "-" + System.currentTimeMillis() + ".png");
            byte[] bytes = page.screenshot(new Page.ScreenshotOptions()
                    .setPath(path)
                    .setFullPage(true));
            log.info("Screenshot saved: {}", path);
            return bytes;
        } catch (IOException e) {
            log.warn("Could not save screenshot: {}", e.getMessage());
            return null;
        }
    }
}
