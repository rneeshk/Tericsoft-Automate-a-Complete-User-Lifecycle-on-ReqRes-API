package tests;

import pojos.UserRequest;
import utils.ConfigReader;
import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("ReqRes API Tests")
@Feature("User Management")
public class UserCreationDataDrivenTest {

    private static RequestSpecification requestSpec;

    @BeforeAll
    static void setup() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getProperty("base.uri"))
                .setBasePath(ConfigReader.getProperty("base.path"))
                .addHeader("x-api-key", ConfigReader.getProperty("api.key"))
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter()) // Log requests
                .addFilter(new ResponseLoggingFilter()) // Log responses
                .addFilter(new AllureRestAssured()) // Allure logging
                .build();
    }

    @Story("Data-Driven User Creation")
    @DisplayName("Create users with data from CSV file")
    @Description("This test verifies that multiple users can be created using data provided from an external CSV file.")
    @ParameterizedTest(name = "Creating user: {0} with job: {1}")
    @CsvFileSource(resources = "/testdata.csv", numLinesToSkip = 1)
    @Step("Execute user creation for name: {name} and job: {job}")
    void testCreateUserWithData(String name, String job) {
        UserRequest userRequest = new UserRequest(name, job);

        given()
                .spec(requestSpec)
                .body(userRequest)
        .when()
                .post("/users")
        .then()
                .statusCode(201)
                .body("name", equalTo(name))
                .body("job", equalTo(job))
                .body("id", notNullValue(String.class));
    }
}