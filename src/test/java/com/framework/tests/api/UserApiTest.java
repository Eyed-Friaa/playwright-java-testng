package com.framework.tests.api;

import com.framework.config.ConfigManager;
import com.framework.models.ApiUserRequest;
import com.framework.utils.JsonUtils;
import com.framework.utils.TestDataFactory;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * API tests using REST Assured against JSONPlaceholder
 * (https://jsonplaceholder.typicode.com) — a stable, auth-free public
 * mock API. Chosen over key-gated mock APIs so the CI pipeline stays
 * green without managing secrets or hitting rate limits.
 *
 * Demonstrates:
 * - REST Assured with a shared RequestSpecification
 * - Allure API test annotations
 * - JSON body validation (Hamcrest matchers)
 * - CRUD operations pattern with typed request models
 *
 * Note: JSONPlaceholder fakes writes — POST/PUT/DELETE return realistic
 * status codes and echo the payload, but nothing is persisted.
 */
@Epic("API")
@Feature("User Management API")
public class UserApiTest {

    private RequestSpecification requestSpec;

    @BeforeClass
    public void setUpSpec() {
        RestAssured.baseURI = ConfigManager.get().apiBaseUrl();

        requestSpec = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .log().ifValidationFails();
    }

    // ─── GET ──────────────────────────────────────────────────────────────────

    @Test(description = "GET /users returns the full user list")
    @Story("Get users")
    @Severity(SeverityLevel.CRITICAL)
    public void getUsersReturnsList() {
        given(requestSpec)
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].id", notNullValue())
            .body("[0].name", notNullValue())
            .body("[0].email", notNullValue());
    }

    @Test(description = "GET /users/{id} returns a single user")
    @Story("Get user by ID")
    @Severity(SeverityLevel.CRITICAL)
    public void getUserByIdReturnsCorrectUser() {
        Response response = given(requestSpec)
            .pathParam("id", 2)
        .when()
            .get("/users/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(2))
            .body("name", notNullValue())
            .body("email", notNullValue())
            .extract().response();

        String email = response.jsonPath().getString("email");
        assertThat(email).isNotBlank();
    }

    @Test(description = "GET /users/{id} with non-existent ID returns 404")
    @Story("Get user by ID")
    @Severity(SeverityLevel.NORMAL)
    public void getNonExistentUserReturns404() {
        given(requestSpec)
        .when()
            .get("/users/9999")
        .then()
            .statusCode(404);
    }

    // ─── POST ─────────────────────────────────────────────────────────────────

    @Test(description = "POST /users creates a new user and returns 201")
    @Story("Create user")
    @Severity(SeverityLevel.BLOCKER)
    public void createUserReturns201WithId() {
        ApiUserRequest user = TestDataFactory.randomUser();
        String requestBody = JsonUtils.toJson(user);

        Response response = given(requestSpec)
            .body(requestBody)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("name", equalTo(user.getName()))
            .body("job", equalTo(user.getJob()))
            .body("id", notNullValue())
            .extract().response();

        String createdId = response.jsonPath().getString("id");
        assertThat(createdId).isNotBlank();
    }

    // ─── PUT ──────────────────────────────────────────────────────────────────

    @Test(description = "PUT /users/{id} updates a user and returns 200")
    @Story("Update user")
    @Severity(SeverityLevel.NORMAL)
    public void updateUserReturns200() {
        ApiUserRequest user = TestDataFactory.randomUser();
        String requestBody = JsonUtils.toJson(user);

        given(requestSpec)
            .body(requestBody)
        .when()
            .put("/users/2")
        .then()
            .statusCode(200)
            .body("name", equalTo(user.getName()));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Test(description = "DELETE /users/{id} returns 200")
    @Story("Delete user")
    @Severity(SeverityLevel.NORMAL)
    public void deleteUserReturns200() {
        given(requestSpec)
        .when()
            .delete("/users/2")
        .then()
            .statusCode(200);
    }
}
