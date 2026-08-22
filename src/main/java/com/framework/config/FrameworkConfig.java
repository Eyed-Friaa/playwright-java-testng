package com.framework.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

/**
 * Typed configuration interface using OWNER library.
 * Values resolved from: system properties > env vars > config.properties
 *
 * Usage: ConfigManager.get().baseUrl()
 */
@LoadPolicy(LoadType.MERGE)
@Sources({
    "system:properties",
    "system:env",
    "classpath:config.properties"
})
public interface FrameworkConfig extends Config {

    @Key("base.url")
    @DefaultValue("https://www.saucedemo.com")
    String baseUrl();

    @Key("browser")
    @DefaultValue("chromium")
    String browser();

    @Key("headless")
    @DefaultValue("true")
    boolean headless();

    @Key("slow.mo")
    @DefaultValue("0")
    int slowMo();

    @Key("default.timeout")
    @DefaultValue("30000")
    int defaultTimeout();

    @Key("screenshot.on.failure")
    @DefaultValue("true")
    boolean screenshotOnFailure();

    @Key("video.on.failure")
    @DefaultValue("false")
    boolean videoOnFailure();

    @Key("trace.on.failure")
    @DefaultValue("true")
    boolean traceOnFailure();

    @Key("viewport.width")
    @DefaultValue("1920")
    int viewportWidth();

    @Key("viewport.height")
    @DefaultValue("1080")
    int viewportHeight();

    @Key("api.base.url")
    @DefaultValue("https://jsonplaceholder.typicode.com")
    String apiBaseUrl();

    @Key("retry.count")
    @DefaultValue("1")
    int retryCount();

    @Key("parallel.threads")
    @DefaultValue("4")
    int parallelThreads();

    @Key("locale")
    @DefaultValue("en-US")
    String locale();

    @Key("timezone.id")
    @DefaultValue("UTC")
    String timezoneId();
}
