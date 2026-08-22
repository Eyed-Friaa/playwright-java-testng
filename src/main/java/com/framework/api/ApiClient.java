package com.framework.api;

import com.framework.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Central entry point for REST Assured request building.
 *
 * Every API test should start a call with {@code ApiClient.spec()} rather
 * than {@code RestAssured.given()} — this guarantees:
 *   • the configured {@code api.base.url} is honored,
 *   • JSON content-type + accept headers are set,
 *   • Allure attaches request/response as steps (via allure-rest-assured),
 *   • request/response are logged only on validation failure — keeping the
 *     console readable on green runs.
 *
 * Auth or per-suite headers belong here too — extend with {@code spec(AuthToken)}
 * overloads rather than reaching into RestAssured statics from tests.
 */
public final class ApiClient {

    private ApiClient() {}

    /** A fresh, pre-configured request builder scoped to this call. */
    public static RequestSpecification spec() {
        return given()
                .spec(baseSpec())
                .log().ifValidationFails();
    }

    /** The underlying spec, exposed for advanced composition (e.g. auth filters). */
    public static RequestSpecification baseSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get().apiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .build();
    }
}
