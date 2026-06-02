package com.framework.utils;

import com.framework.models.ApiUserRequest;
import net.datafaker.Faker;

import java.util.Locale;

/**
 * Central factory for randomized, realistic test data.
 * Uses datafaker with a German locale to match the framework's context.
 *
 * Randomized data surfaces edge cases that hard-coded fixtures hide,
 * and prevents tests from silently depending on specific literal values.
 */
public final class TestDataFactory {

    private static final Faker FAKER = new Faker(Locale.GERMAN);

    private TestDataFactory() {}

    public static ApiUserRequest randomUser() {
        return ApiUserRequest.builder()
                .name(FAKER.name().fullName())
                .job(FAKER.job().title())
                .build();
    }

    public static String randomEmail() {
        return FAKER.internet().emailAddress();
    }

    public static String randomCompany() {
        return FAKER.company().name();
    }
}
