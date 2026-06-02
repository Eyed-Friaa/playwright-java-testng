package com.framework.config;

import org.aeonbits.owner.ConfigCache;

/**
 * Singleton access point for all framework configuration.
 * Thread-safe via OWNER ConfigCache.
 */
public final class ConfigManager {

    private ConfigManager() {}

    public static FrameworkConfig get() {
        return ConfigCache.getOrCreate(FrameworkConfig.class);
    }
}
