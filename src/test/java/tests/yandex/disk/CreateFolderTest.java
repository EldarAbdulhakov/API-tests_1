package tests.yandex.disk;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateFolderTest extends BaseYandexDiscTest {

    @Test
    public void testCreateFolderInRootDirectory() {
        String folderName = uniqueFolderName.get();
        registerCreatedResource(folderName);

        given()
                .spec(requestSpec)
                .put(RESOURCE_PATH.formatted(folderName))
                .then()
                .log().all()
                .statusCode(201)
                .body("method", equalTo("GET"))
                .body("href", equalTo("https://cloud-api.yandex.net/v1/disk/resources?path=disk%%3A%%2F%s".formatted(folderName)))
                .body("templated", equalTo(false));
    }

    @Test
    public void testCreateNestedFolder() {
        String folderName = uniqueFolderName.get();
        createFolder(folderName);

        given()
                .spec(requestSpec)
                .put(NESTED_RESOURCE_PATH.formatted(folderName, NESTED_FOLDER))
                .then()
                .log().all()
                .statusCode(201)
                .body("method", equalTo("GET"))
                .body("href", equalTo("https://cloud-api.yandex.net/v1/disk/resources?path=disk%%3A%%2F%s%%2F%s".formatted(folderName, NESTED_FOLDER)))
                .body("templated", equalTo(false));
    }

    @Test
    public void testCreateFolderWithExistingName() {
        String folderName = uniqueFolderName.get();
        createFolder(folderName);

        given()
                .spec(requestSpec)
                .put(RESOURCE_PATH.formatted(folderName))
                .then()
                .log().all()
                .statusCode(409)
                .body("error", equalTo("DiskPathPointsToExistentDirectoryError"))
                .body("description", equalTo("Specified path \"%s\" points to existent directory.".formatted(folderName)))
                .body("message", equalTo("По указанному пути \"%s\" уже существует папка с таким именем.".formatted(folderName)));
    }

    @Test
    public void testCreateFolderWithoutOAuthToken() {
        String folderName = uniqueFolderName.get();

        given()
                .spec(requestSpecWithoutAuth)
                .put(RESOURCE_PATH.formatted(folderName))
                .then()
                .log().all()
                .statusCode(401)
                .body("error", equalTo("UnauthorizedError"))
                .body("description", equalTo("Unauthorized"))
                .body("message", equalTo("Не авторизован."));
    }
}
