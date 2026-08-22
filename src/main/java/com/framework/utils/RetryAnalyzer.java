package com.framework.utils;

import com.framework.config.ConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * TestNG retry analyzer — retries failed tests up to config retry.count times.
 *
 * Usage on individual test:
 *   @Test(retryAnalyzer = RetryAnalyzer.class)
 *
 * Usage globally via RetryListener (preferred) — see TestNG listener config.
 */
@Slf4j
public class RetryAnalyzer implements IRetryAnalyzer {

    // TestNG creates one RetryAnalyzer instance per test method, but reuses it
    // across every @DataProvider row of that method. Keying attempts by the
    // per-invocation identity prevents retry budget from leaking between rows.
    private final java.util.Map<String, Integer> attemptsByInvocation =
            new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public boolean retry(ITestResult result) {
        int maxRetry = ConfigManager.get().retryCount();
        String key = invocationKey(result);
        int attempt = attemptsByInvocation.merge(key, 1, Integer::sum);
        if (attempt <= maxRetry) {
            log.warn("Retrying test '{}' — attempt {}/{}", result.getName(), attempt, maxRetry);
            return true;
        }
        // Budget exhausted for this invocation — reclaim to keep the map bounded.
        attemptsByInvocation.remove(key);
        return false;
    }

    private static String invocationKey(ITestResult result) {
        Object[] params = result.getParameters();
        return result.getMethod().getQualifiedName() + '#' + java.util.Arrays.deepToString(params);
    }
}
