package com.framework.hooks;

import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Global TestNG listener — suite/test lifecycle logging and Allure
 * environment wiring.
 *
 * NOTE: retry handling lives in {@link RetryListener} (an
 * IAnnotationTransformer). Both are registered in the suite XML files.
 */
@Slf4j
public class TestListener implements ITestListener, ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        log.info("═══ Suite started: {} ═══", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("═══ Suite finished: {} ═══", suite.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("── Test context: {} ──", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info("▶ {}.{}", result.getTestClass().getRealClass().getSimpleName(), result.getName());
        Allure.getLifecycle().updateTestCase(tc -> tc.setName(result.getName()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✓ PASSED  — {} ({} ms)", result.getName(),
                result.getEndMillis() - result.getStartMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable t = result.getThrowable();
        log.error("✗ FAILED  — {} | {}", result.getName(),
                t != null ? t.getMessage() : "unknown cause");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("↷ SKIPPED — {}", result.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.warn("≈ FLAKY   — {} (passed within success percentage)", result.getName());
    }
}
