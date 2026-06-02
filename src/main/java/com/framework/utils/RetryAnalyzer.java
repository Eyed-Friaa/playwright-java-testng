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

    private int currentRetry = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetry = ConfigManager.get().retryCount();
        if (currentRetry < maxRetry) {
            currentRetry++;
            log.warn("Retrying test '{}' — attempt {}/{}", 
                result.getName(), currentRetry, maxRetry);
            return true;
        }
        return false;
    }
}
