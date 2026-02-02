package tests.yandex.disk;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateFileTest extends BaseYandexDiscTest {

    @Test
    public void testCreateTextFileInRoot() {
        String fileName = uniqueFileName.get();

        String href = given()
                .spec(requestSpec)
                .get(UPLOAD_RESOURCE_PATH.formatted(fileName))
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
        String folderName = uniqueFolderName.get();
        String fileName = uniqueFileName.get();
        createFolder(folderName);

        String href = given()
                .spec(requestSpec)
                .get(NESTED_UPLOAD_RESOURCE_PATH.formatted(folderName, fileName))
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
        String fileName = uniqueFileName.get();

        given()
                .spec(requestSpecWithoutAuth)
                .get(UPLOAD_RESOURCE_PATH.formatted(fileName))
                .then()
                .statusCode(401)
                .body("error", equalTo("UnauthorizedError"))
                .body("description", equalTo("Unauthorized"))
                .body("message", equalTo("Не авторизован."));
    }
}
