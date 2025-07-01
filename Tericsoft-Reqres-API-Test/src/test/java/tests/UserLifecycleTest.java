package tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.javafaker.Faker;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import pojos.UserRequest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserLifecycleTest {

    // A static RequestSpecification to hold setup common to all requests.
    private static RequestSpecification requestSpec;

    // Static variables to share data between the ordered tests.
    private static String userId;
    private static String userName;
    private static String userJob;

    // Instance of Faker to generate dynamic test data.
    private Faker faker = new Faker();

    @BeforeAll
    static void setup() {
        // Build the RequestSpecification once for all tests.
        requestSpec = new RequestSpecBuilder()
                .setBaseUri("https://reqres.in")
                .setBasePath("/api")
                
                // Add the required API key header for authentication.
                
                .addHeader("x-api-key", "reqres-free-v1")
                .setContentType(ContentType.JSON)
                .build();
    }

    @Order(1)
    @Story("Full User Lifecycle")
    @DisplayName("1. Create a new user with dynamic data")
    @Description("Verify that a new user can be created with a random name and job title.")
    @Step("Create a new user")
    void testCreateUser() {
        userName = faker.name().fullName();
        userJob = faker.job().title();
        UserRequest userRequest = new UserRequest(userName, userJob);

        userId = given()
                .spec(requestSpec)
                .body(userRequest)
        .when()
                .post("/users")
        .then()
                .statusCode(201)
                .body("name", equalTo(userName))
                .extract().path("id");
    }

    @Test
    @Order(2)
    @Story("Full User Lifecycle")
    @DisplayName("2. Update the user's job title")
    @Description("Verify that an existing user's job can be updated.")
    @Step("Update the user's job")
    void testUpdateUser() {
        Assertions.assertNotNull(userId, "User ID from create test is required.");
        String updatedJob = faker.job().title();
        UserRequest updateUserRequest = new UserRequest(userName, updatedJob);

        given()
                .spec(requestSpec)
                .body(updateUserRequest)
        .when()
                .put("/users/" + userId)
        .then()
                .statusCode(200)
                .body("job", equalTo(updatedJob));
    }

    @Test
    @Order(3)
    @DisplayName("3. Get a known user to validate the GET endpoint")
    void testGetUser() {

    	// This test no longer depends on the created userId, because that ID doesn't really exist.
        // Instead, we test the GET endpoint's contract using a known, valid user ID from ReqRes's mock data.
    	// Use a known, valid ID from the ReqRes documentation.
        int knownUserId = 2; 

        System.out.println("\n---- Step 3: GET a Known User ----");
        System.out.println("Endpoint: /users/" + knownUserId);

        // Send GET request and validate the structure of the known user's data.
        given()
                .spec(requestSpec)
                .pathParam("id", knownUserId)
        .when()
                .get("/users/{id}")
        .then()
                .log().all()
                // Validate the status code is 200 OK.
                .statusCode(200)
                // Validate the response body contains the correct data for the known user.
                .body("data.id", equalTo(knownUserId))
                .body("data.email", equalTo("janet.weaver@reqres.in"))
                .body("data.first_name", equalTo("Janet"))
                .body("data.last_name", equalTo("Weaver"));

        System.out.println("Successfully validated GET request for known user ID: " + knownUserId);
    }


    @Test
    @Order(4)
    @DisplayName("4. Delete the user")
    void testDeleteUser() {
        Assertions.assertNotNull(userId, "User ID from previous test is required.");

        System.out.println("\n---- Step 4: DELETE User ----");

        // Send the DELETE request and validate the response
        given()
                .spec(requestSpec)
        .when()
                .delete("/users/" + userId)
        .then()
                .log().all()
                .statusCode(204);

        System.out.println("Successfully deleted user with ID: " + userId);
    }

    @Test
    @Order(5)
    @DisplayName("Negative: Get a user with an invalid ID")
    void testGetNonExistentUser() {
        String invalidId = "99999";
        System.out.println("\n---- Negative Test: GET Non-Existent User ----");

        // Send GET request for an ID that doesn't exist
        given()
                .spec(requestSpec)
        .when()
                .get("/users/" + invalidId)
        .then()
                .log().all()
                .statusCode(404);

        System.out.println("Validated that GET request for an invalid user ID returns 404.");
    }
}