package com.framework.tests.api;

import com.framework.api.endpoints.UsersApi;
import com.framework.models.ApiUserRequest;
import com.framework.utils.TestDataFactory;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API tests using REST Assured against JSONPlaceholder
 * (https://jsonplaceholder.typicode.com) — a stable, auth-free public
 * mock API. Chosen so the CI pipeline stays green without managing
 * secrets or hitting rate limits.
 *
 * All HTTP mechanics live in {@link UsersApi} / {@link com.framework.api.ApiClient}.
 * Tests only describe intent + assertions.
 *
 * Note: JSONPlaceholder fakes writes — POST/PUT/DELETE return realistic
 * status codes and echo the payload, but nothing is persisted.
 */
@Epic("API")
@Feature("User Management API")
public class UserApiTest {

    // ─── GET ──────────────────────────────────────────────────────────────────

    @Test(description = "GET /users returns the full user list")
    @Story("Get users")
    @Severity(SeverityLevel.CRITICAL)
    public void getUsersReturnsList() {
        Response response = UsersApi.getAll();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("$")).isNotEmpty();
        assertThat(response.jsonPath().getString("[0].id")).isNotBlank();
        assertThat(response.jsonPath().getString("[0].name")).isNotBlank();
        assertThat(response.jsonPath().getString("[0].email")).isNotBlank();
    }

    @Test(description = "GET /users/{id} returns a single user")
    @Story("Get user by ID")
    @Severity(SeverityLevel.CRITICAL)
    public void getUserByIdReturnsCorrectUser() {
        Response response = UsersApi.getById(2);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("id")).isEqualTo(2);
        assertThat(response.jsonPath().getString("email")).isNotBlank();
    }

    @Test(description = "GET /users/{id} with non-existent ID returns 404")
    @Story("Get user by ID")
    @Severity(SeverityLevel.NORMAL)
    public void getNonExistentUserReturns404() {
        Response response = UsersApi.getById(9999);
        assertThat(response.statusCode()).isEqualTo(404);
    }

    // ─── POST ─────────────────────────────────────────────────────────────────

    @Test(description = "POST /users creates a new user and returns 201")
    @Story("Create user")
    @Severity(SeverityLevel.BLOCKER)
    public void createUserReturns201WithId() {
        ApiUserRequest user = TestDataFactory.randomUser();

        Response response = UsersApi.create(user);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("name")).isEqualTo(user.getName());
        assertThat(response.jsonPath().getString("job")).isEqualTo(user.getJob());
        assertThat(response.jsonPath().getString("id")).isNotBlank();
    }

    // ─── PUT ──────────────────────────────────────────────────────────────────

    @Test(description = "PUT /users/{id} updates a user and returns 200")
    @Story("Update user")
    @Severity(SeverityLevel.NORMAL)
    public void updateUserReturns200() {
        ApiUserRequest user = TestDataFactory.randomUser();

        Response response = UsersApi.update(2, user);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("name")).isEqualTo(user.getName());
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Test(description = "DELETE /users/{id} returns 200")
    @Story("Delete user")
    @Severity(SeverityLevel.NORMAL)
    public void deleteUserReturns200() {
        Response response = UsersApi.delete(2);
        assertThat(response.statusCode()).isEqualTo(200);
    }
}
