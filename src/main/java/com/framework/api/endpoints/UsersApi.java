package com.framework.api.endpoints;

import com.framework.api.ApiClient;
import com.framework.models.ApiUserRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

/**
 * Endpoint client for /users on the target API.
 *
 * Pattern: one class per resource. Methods return {@link Response} so tests
 * still own their assertions (avoids the common trap where the endpoint client
 * asserts a happy-path status and negative tests can't reuse it).
 */
public final class UsersApi {

    private static final String USERS_PATH = "/users";

    private UsersApi() {}

    @Step("GET /users")
    public static Response getAll() {
        return ApiClient.spec()
                .when().get(USERS_PATH);
    }

    @Step("GET /users/{id}")
    public static Response getById(int id) {
        return ApiClient.spec()
                .pathParam("id", id)
                .when().get(USERS_PATH + "/{id}");
    }

    @Step("POST /users")
    public static Response create(ApiUserRequest user) {
        return ApiClient.spec()
                .body(user)
                .when().post(USERS_PATH);
    }

    @Step("PUT /users/{id}")
    public static Response update(int id, ApiUserRequest user) {
        return ApiClient.spec()
                .pathParam("id", id)
                .body(user)
                .when().put(USERS_PATH + "/{id}");
    }

    @Step("DELETE /users/{id}")
    public static Response delete(int id) {
        return ApiClient.spec()
                .pathParam("id", id)
                .when().delete(USERS_PATH + "/{id}");
    }
}
