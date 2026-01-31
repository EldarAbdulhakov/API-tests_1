package tests.yandex.disk;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class DeleteFolderTest extends BaseYandexDiscTest {

    @Test
    public void testDeleteFolderToTrash() {
        String folderName = uniqueFolderName.get();
        createFolder(folderName);

        given()
                .spec(requestSpec)
                .delete(RESOURCE_PATH.formatted(folderName))
                .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void testDeleteFolderPermanently() {
        String folderName = uniqueFolderName.get();
        createFolder(folderName);

        given()
                .spec(requestSpec)
                .delete(RESOURCE_PATH_PERMANENTLY.formatted(folderName))
                .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void testDeleteFolderWithNestedFolderToTrash() {
        String folderName = uniqueFolderName.get();
        createFolder(folderName);
        createFolder(folderName + "/" + NESTED_FOLDER);

        given()
                .spec(requestSpec)
                .delete(RESOURCE_PATH_PERMANENTLY.formatted(folderName))
                .then()
                .log().all()
                .statusCode(202)
                .body("method", equalTo("GET"))
                .body("href", containsString("https://cloud-api.yandex.net/v1/disk/operations/"))
                .body("templated", equalTo(false));
    }

    @Test
    public void testDeleteNonExistentFolderToTrash() {
        String folderName = uniqueFolderName.get();

        given()
                .spec(requestSpec)
                .delete(RESOURCE_PATH.formatted(folderName))
                .then()
                .log().all()
                .statusCode(404)
                .body("error", equalTo("DiskNotFoundError"))
                .body("description", equalTo("Resource not found."))
                .body("message", equalTo("Не удалось найти запрошенный ресурс."));
    }

    @Test
    public void testDeleteFolderWithoutOAuthToken() {
        String folderName = uniqueFolderName.get();
        createFolder(folderName);

        given()
                .spec(requestSpecWithoutAuth)
                .delete(RESOURCE_PATH.formatted(folderName))
                .then()
                .log().all()
                .statusCode(401)
                .body("error", equalTo("UnauthorizedError"))
                .body("description", equalTo("Unauthorized"))
                .body("message", equalTo("Не авторизован."));
    }
}
