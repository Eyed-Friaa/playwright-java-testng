package com.framework.tests;

import com.framework.utils.BrowserManager;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

/**
 * Base class for all UI tests.
 *
 * Split lifecycle for perf: Playwright + Browser are launched once per class
 * (~1s saved per test), while each @Test gets its own fresh BrowserContext
 * + Page (still fully isolated — no cookies/storage bleed).
 *
 * Retry is applied globally via RetryListener — no per-test config needed.
 */
@Slf4j
public abstract class BaseTest {

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        BrowserManager.initSharedBrowser();
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        BrowserManager.tearDownSharedBrowser();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        log.info("▶ Starting test: {}", method.getName());
        BrowserManager.initTestContext(method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        boolean passed = result.getStatus() == ITestResult.SUCCESS;
        String testName = result.getMethod().getMethodName();

        if (passed) {
            log.info("✓ PASSED: {}", testName);
        } else {
            log.error("✗ FAILED: {}", testName);
        }

        BrowserManager.tearDownTestContext(passed, testName);
    }
}
