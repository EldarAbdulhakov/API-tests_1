package tests.yandex.disk;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateFileTest extends BaseYandexDiscTest {

    @Test
    public void testCreateTextFileInRoot() {
        String href = given()
                .spec(requestSpec)
                .get("v1/disk/resources/upload?path=file.txt")
                .then()
                .extract().path("href");

        given()
                .spec(requestSpec)
                .put(href)
                .then()
                .statusCode(201);
    }

    @Test
    public void testCreateTextFileInFolder() {
        createFolder(FOLDER_NAME);

        String href = given()
                .spec(requestSpec)
                .get("v1/disk/resources/upload?path=%s/file.txt".formatted(FOLDER_NAME))
                .then()
                .extract().path("href");

        given()
                .spec(requestSpec)
                .put(href)
                .then()
                .statusCode(201);
    }

    @Test
    public void testCreateTextFileWithoutOAuthToken() {
        given()
                .spec(requestSpecWithoutAuth)
                .get("v1/disk/resources/upload?path=file.txt")
                .then()
                .statusCode(401)
                .body("error", equalTo("UnauthorizedError"))
                .body("description", equalTo("Unauthorized"))
                .body("message", equalTo("Не авторизован."));
    }
}
