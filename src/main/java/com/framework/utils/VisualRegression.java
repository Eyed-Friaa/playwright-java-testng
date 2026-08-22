package com.framework.utils;

import com.framework.config.ConfigManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ScreenshotAnimations;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Byte-level visual regression against baselines committed to the repo.
 *
 * Baseline layout — one folder per browser to sidestep AA differences:
 *   src/test/resources/screenshots/{browser}/{name}.png
 *
 * First run for a new (browser, name) writes the baseline and passes —
 * commit the file and subsequent runs compare against it.
 *
 * On mismatch: the actual capture is dumped to
 *   target/visual-diffs/{name}.actual.png
 * and attached to the Allure report next to the baseline for easy triage.
 *
 * <b>Cross-platform note.</b> Font antialiasing differs between Linux (CI)
 * and macOS/Windows (dev), so baselines are effectively CI artifacts.
 * Regenerate locally with {@code -Dvisual.update=true} when you intend to.
 */
@Slf4j
public final class VisualRegression {

    private static final String BASELINE_ROOT = "src/test/resources/screenshots";
    private static final String DIFF_ROOT     = "target/visual-diffs";
    private static final String UPDATE_PROP   = "visual.update";

    private VisualRegression() {}

    /** Full-page screenshot compared against baseline. */
    public static void assertPageMatches(Page page, String name) {
        byte[] actual = page.screenshot(new Page.ScreenshotOptions()
                .setFullPage(true)
                .setAnimations(ScreenshotAnimations.DISABLED));
        compare(actual, name);
    }

    /** Element screenshot compared against baseline — preferred for stability. */
    public static void assertLocatorMatches(Locator locator, String name) {
        byte[] actual = locator.screenshot(new Locator.ScreenshotOptions()
                .setAnimations(ScreenshotAnimations.DISABLED));
        compare(actual, name);
    }

    // ─── internals ────────────────────────────────────────────────────────────

    private static void compare(byte[] actual, String name) {
        String browser = ConfigManager.get().browser().toLowerCase();
        Path baseline  = Paths.get(BASELINE_ROOT, browser, name + ".png");
        boolean updateMode = Boolean.getBoolean(UPDATE_PROP);

        try {
            if (!Files.exists(baseline) || updateMode) {
                Files.createDirectories(baseline.getParent());
                Files.write(baseline, actual);
                Allure.addAttachment("Baseline written — " + name, "image/png",
                        new ByteArrayInputStream(actual), "png");
                log.info("Visual baseline {} for '{}' at {}",
                        updateMode ? "updated" : "created", name, baseline);
                return;
            }

            byte[] expected = Files.readAllBytes(baseline);
            if (java.util.Arrays.equals(expected, actual)) {
                log.debug("Visual match: {}", name);
                return;
            }

            Path diffPath = Paths.get(DIFF_ROOT, name + ".actual.png");
            Files.createDirectories(diffPath.getParent());
            Files.write(diffPath, actual);

            Allure.addAttachment("Expected — " + name, "image/png",
                    new ByteArrayInputStream(expected), "png");
            Allure.addAttachment("Actual — "   + name, "image/png",
                    new ByteArrayInputStream(actual),   "png");

            Assertions.fail(String.format(
                    "Visual regression '%s' failed on %s. Actual saved to %s. "
                    + "Regenerate with -Dvisual.update=true if the change is intentional.",
                    name, browser, diffPath));
        } catch (IOException e) {
            throw new RuntimeException("Visual regression IO failure for " + name, e);
        }
    }
}
