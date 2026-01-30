package tests.yandex.disk;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateFolderTest extends BaseYandexDiscTest {

    @Test
    public void testCreateFolderInRootDirectory() {
        registerCreatedResource(FOLDER_NAME);

        given()
                .spec(requestSpec)
                .put("v1/disk/resources?path=%s".formatted(FOLDER_NAME))
                .then()
                .log().all()
                .statusCode(201)
                .body("method", equalTo("GET"))
                .body("href", equalTo("https://cloud-api.yandex.net/v1/disk/resources?path=disk%%3A%%2F%s".formatted(FOLDER_NAME)))
                .body("templated", equalTo(false));
    }

    @Test
    public void testCreateNestedFolder() {
        createFolder(FOLDER_NAME);

        given()
                .spec(requestSpec)
                .put("v1/disk/resources?path=%s/%s".formatted(FOLDER_NAME, NESTED_FOLDER))
                .then()
                .log().all()
                .statusCode(201)
                .body("method", equalTo("GET"))
                .body("href", equalTo("https://cloud-api.yandex.net/v1/disk/resources?path=disk%%3A%%2F%s%%2F%s".formatted(FOLDER_NAME, NESTED_FOLDER)))
                .body("templated", equalTo(false));
    }

    @Test
    public void testCreateFolderWithExistingName() {
        createFolder(FOLDER_NAME);

        given()
                .spec(requestSpec)
                .put("v1/disk/resources?path=%s".formatted(FOLDER_NAME))
                .then()
                .log().all()
                .statusCode(409)
                .body("error", equalTo("DiskPathPointsToExistentDirectoryError"))
                .body("description", equalTo("Specified path \"%s\" points to existent directory.".formatted(FOLDER_NAME)))
                .body("message", equalTo("По указанному пути \"%s\" уже существует папка с таким именем.".formatted(FOLDER_NAME)));
    }

    @Test
    public void testCreateFolderWithoutOAuthToken() {
        given()
                .spec(requestSpecWithoutAuth)
                .put("v1/disk/resources?path=%s".formatted(FOLDER_NAME))
                .then()
                .log().all()
                .statusCode(401)
                .body("error", equalTo("UnauthorizedError"))
                .body("description", equalTo("Unauthorized"))
                .body("message", equalTo("Не авторизован."));
    }
}
