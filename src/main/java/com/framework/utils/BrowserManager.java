package com.framework.utils;

import com.framework.config.ConfigManager;
import com.framework.config.FrameworkConfig;
import com.microsoft.playwright.*;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Thread-local Playwright lifecycle manager.
 *
 * Split lifecycle (recommended Playwright pattern):
 *   - {@link #initSharedBrowser()}  → once per thread (per class), reuses Playwright + Browser
 *   - {@link #initTestContext(String)} → per test method, fresh BrowserContext + Page
 *   - {@link #tearDownTestContext(boolean, String)} → per test method
 *   - {@link #tearDownSharedBrowser()} → once per thread (per class)
 *
 * BaseTest wires these to @BeforeClass/@AfterClass and @BeforeMethod/@AfterMethod.
 * A per-thread Browser is safe under TestNG parallel="methods" because each
 * thread owns its own stack; a per-thread Browser under parallel="classes" is
 * strictly better (one browser process per class, not per method).
 */
@Slf4j
public final class BrowserManager {

    private static final ThreadLocal<Playwright>     playwrightTL = new ThreadLocal<>();
    private static final ThreadLocal<Browser>        browserTL    = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextTL    = new ThreadLocal<>();
    private static final ThreadLocal<Page>           pageTL       = new ThreadLocal<>();

    private BrowserManager() {}

    // ─── Shared lifecycle (per class / per thread) ────────────────────────────

    /** Idempotent: safe to call from @BeforeClass or @BeforeMethod. */
    public static void initSharedBrowser() {
        if (browserTL.get() != null) return;

        FrameworkConfig cfg = ConfigManager.get();
        log.info("Starting browser: {} | headless={}", cfg.browser(), cfg.headless());

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
    }

    public static void tearDownSharedBrowser() {
        closeSilently(browserTL.get());
        closeSilently(playwrightTL.get());
        browserTL.remove();
        playwrightTL.remove();
        log.debug("Browser destroyed on thread {}", Thread.currentThread().getId());
    }

    // ─── Per-test lifecycle ───────────────────────────────────────────────────

    public static void initTestContext(String testName) {
        FrameworkConfig cfg = ConfigManager.get();
        Browser browser = browserTL.get();
        if (browser == null) {
            // Belt-and-braces: callable directly from @BeforeMethod with no @BeforeClass.
            initSharedBrowser();
            browser = browserTL.get();
        }

        NewContextOptions contextOptions = new NewContextOptions()
                .setViewportSize(cfg.viewportWidth(), cfg.viewportHeight())
                .setIgnoreHTTPSErrors(true)
                .setLocale(cfg.locale())
                .setTimezoneId(cfg.timezoneId());

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
    }

    /** Captures artifacts on failure and always closes the per-test context. */
    public static void tearDownTestContext(boolean testPassed, String testName) {
        FrameworkConfig cfg = ConfigManager.get();
        BrowserContext context = contextTL.get();
        try {
            if (!testPassed) {
                captureFailureArtifacts(cfg, testName);
            } else if (cfg.traceOnFailure() && context != null) {
                // Stop tracing on pass without writing to disk — otherwise the buffer
                // leaks memory when the context lives inside a long-running Browser.
                context.tracing().stop();
            }
        } catch (Exception e) {
            log.warn("Error during failure-artifact capture: {}", e.getMessage());
        } finally {
            closeSilently(pageTL.get());
            closeSilently(contextTL.get());
            pageTL.remove();
            contextTL.remove();
        }
    }

    private static void captureFailureArtifacts(FrameworkConfig cfg, String testName) {
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

    // ─── Deprecated single-shot API (kept for existing tests) ─────────────────

    /** @deprecated use {@link #initSharedBrowser()} + {@link #initTestContext(String)}. */
    @Deprecated
    public static void initBrowser() {
        initSharedBrowser();
        initTestContext("legacy");
    }

    /** @deprecated use {@link #tearDownTestContext(boolean, String)} + {@link #tearDownSharedBrowser()}. */
    @Deprecated
    public static void tearDownBrowser(boolean testPassed, String testName) {
        tearDownTestContext(testPassed, testName);
        tearDownSharedBrowser();
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public static Page getPage() {
        Page page = pageTL.get();
        if (page == null) {
            throw new IllegalStateException("Page not initialised — call BrowserManager.initTestContext() first");
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
