package com.framework.tests;

import com.framework.utils.BrowserManager;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

/**
 * Base class for all UI tests.
 *
 * Responsibilities:
 * - Browser lifecycle (@BeforeMethod / @AfterMethod)
 * - Delegates failure capture (screenshot + Allure attachment + trace)
 *   to {@link BrowserManager#tearDownBrowser}, so it happens in exactly
 *   one place.
 *
 * Retry is applied globally via RetryListener — no per-test config needed.
 */
@Slf4j
public abstract class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        log.info("▶ Starting test: {}", method.getName());
        BrowserManager.initBrowser();
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

        BrowserManager.tearDownBrowser(passed, testName);
    }
}
