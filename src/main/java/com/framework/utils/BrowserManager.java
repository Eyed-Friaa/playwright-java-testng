package com.framework.utils;

import com.framework.config.ConfigManager;
import com.microsoft.playwright.*;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Browser.NewContextOptions;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Thread-local Playwright lifecycle manager.
 * Each thread owns its own Playwright → Browser → BrowserContext → Page stack.
 * Safe for TestNG parallel execution (methods or classes).
 */
@Slf4j
public final class BrowserManager {

    private static final ThreadLocal<Playwright> playwrightTL  = new ThreadLocal<>();
    private static final ThreadLocal<Browser>    browserTL     = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextTL = new ThreadLocal<>();
    private static final ThreadLocal<Page>       pageTL        = new ThreadLocal<>();

    private BrowserManager() {}

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    public static void initBrowser() {
        var cfg = ConfigManager.get();
        log.info("Initialising browser: {} | headless={}", cfg.browser(), cfg.headless());

        Playwright playwright = Playwright.create();
        playwrightTL.set(playwright);

        LaunchOptions launchOptions = new LaunchOptions()
                .setHeadless(cfg.headless())
                .setSlowMo(cfg.slowMo())
                .setArgs(Arrays.asList(
                        "--no-sandbox",
                        "--disable-setuid-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-gpu"
                ));

        Browser browser = switch (cfg.browser().toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(launchOptions);
            case "webkit"  -> playwright.webkit().launch(launchOptions);
            default        -> playwright.chromium().launch(launchOptions);
        };
        browserTL.set(browser);

        NewContextOptions contextOptions = new NewContextOptions()
                .setViewportSize(cfg.viewportWidth(), cfg.viewportHeight())
                .setIgnoreHTTPSErrors(true)
                .setLocale("de-DE")
                .setTimezoneId("Europe/Berlin");

        if (cfg.videoOnFailure()) {
            contextOptions.setRecordVideoDir(Paths.get("target/videos/"));
        }

        BrowserContext context = browser.newContext(contextOptions);
        context.setDefaultTimeout(cfg.defaultTimeout());
        context.setDefaultNavigationTimeout(cfg.defaultTimeout());

        if (cfg.traceOnFailure()) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        }

        contextTL.set(context);
        pageTL.set(context.newPage());

        log.debug("Browser stack initialised on thread {}", Thread.currentThread().getId());
    }

    /**
     * Tears down the browser stack. On failure, captures a single screenshot
     * (saved to disk + attached to Allure) and stops the Playwright trace.
     */
    public static void tearDownBrowser(boolean testPassed, String testName) {
        var cfg = ConfigManager.get();
        try {
            if (!testPassed) {
                captureFailureArtifacts(cfg, testName);
            }
        } catch (Exception e) {
            log.warn("Error during failure-artifact capture: {}", e.getMessage());
        } finally {
            closeSilently(pageTL.get());
            closeSilently(contextTL.get());
            closeSilently(browserTL.get());
            closeSilently(playwrightTL.get());

            pageTL.remove();
            contextTL.remove();
            browserTL.remove();
            playwrightTL.remove();

            log.debug("Browser stack destroyed on thread {}", Thread.currentThread().getId());
        }
    }

    private static void captureFailureArtifacts(com.framework.config.FrameworkConfig cfg, String testName) {
        Page page = pageTL.get();
        BrowserContext context = contextTL.get();

        if (cfg.screenshotOnFailure() && page != null) {
            byte[] png = ScreenshotUtils.capture(page, testName);
            if (png != null) {
                Allure.addAttachment(
                        "Screenshot on failure — " + testName,
                        "image/png",
                        new ByteArrayInputStream(png),
                        "png");
            }
        }

        if (cfg.traceOnFailure() && context != null) {
            String tracePath = "target/traces/" + testName + "-" + System.currentTimeMillis() + ".zip";
            context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get(tracePath)));
            log.info("Trace saved: {} (inspect at trace.playwright.dev)", tracePath);
        }
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public static Page getPage() {
        Page page = pageTL.get();
        if (page == null) {
            throw new IllegalStateException("Page not initialised — call BrowserManager.initBrowser() first");
        }
        return page;
    }

    public static BrowserContext getContext() {
        return contextTL.get();
    }

    public static Browser getBrowser() {
        return browserTL.get();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static void closeSilently(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.warn("Error closing resource: {}", e.getMessage());
            }
        }
    }
}
