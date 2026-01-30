package tests.yandex.disk;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class RecoveryFolderTest extends BaseYandexDiscTest {

    @Test
    public void testRestoreFolderFromTrash() {
        createFolder(FOLDER_NAME);
        deleteFolderToTrash(FOLDER_NAME);

        List<Map<String, Object>> items = given()
                .spec(requestSpec)
                .get("v1/disk/trash/resources")
                .then()
                .extract().path("_embedded.items");

        String folderPath = items.stream()
                .filter(item -> FOLDER_NAME.equals(item.get("name")))
                .map(item -> (String) item.get("path"))
                .findFirst()
                .orElse(null);

        Response response = given()
                .spec(requestSpec)
                .put("v1/disk/trash/resources/restore?path=%s".formatted(folderPath))
                .then()
                .log().all()
                .extract().response();

        int statusCode = response.statusCode();

        if (statusCode == 201) {
            response.then()
                    .body("method", equalTo("GET"))
                    .body("href", equalTo("https://cloud-api.yandex.net/v1/disk/resources?path=disk%%3A%%2F%s".formatted(FOLDER_NAME)))
                    .body("templated", equalTo(false));
        } else if (statusCode == 202) {
            response.then()
                    .body("method", equalTo("GET"))
                    .body("href", containsString("https://cloud-api.yandex.net/v1/disk/operations/"))
                    .body("templated", equalTo(false));
        }
    }

    @Test
    public void testRestoreFolderWithNestedFolderFromTrash() {
        createFolder(FOLDER_NAME);
        createFolder(FOLDER_NAME + "/" + NESTED_FOLDER);
        deleteFolderToTrash(FOLDER_NAME);

        List<Map<String, Object>> items = given()
                .spec(requestSpec)
                .get("v1/disk/trash/resources")
                .then()
                .extract().path("_embedded.items");

        String folderPath = items.stream()
                .filter(item -> FOLDER_NAME.equals(item.get("name")))
                .map(item -> (String) item.get("path"))
                .findFirst()
                .orElse(null);

        Response response = given()
                .spec(requestSpec)
                .put("v1/disk/trash/resources/restore?path=%s".formatted(folderPath))
                .then()
                .log().all()
                .extract().response();

        int statusCode = response.statusCode();

        if (statusCode == 201) {
            response.then()
                    .body("method", equalTo("GET"))
                    .body("href", equalTo("https://cloud-api.yandex.net/v1/disk/resources?path=disk%%3A%%2F%s".formatted(FOLDER_NAME)))
                    .body("templated", equalTo(false));
        } else if (statusCode == 202) {
            response.then()
                    .body("method", equalTo("GET"))
                    .body("href", containsString("https://cloud-api.yandex.net/v1/disk/operations/"))
                    .body("templated", equalTo(false));
        }
    }

    @Test
    public void testRestoreFolderFromTrashWithoutAuthToken() {
        createFolder(FOLDER_NAME);
        deleteFolderToTrash(FOLDER_NAME);

        List<Map<String, Object>> items = given()
                .spec(requestSpec)
                .get("v1/disk/trash/resources")
                .then()
                .extract().path("_embedded.items");

        String folderPath = items.stream()
                .filter(item -> FOLDER_NAME.equals(item.get("name")))
                .map(item -> (String) item.get("path"))
                .findFirst()
                .orElse(null);

        given()
                .spec(requestSpec)
                .put("v1/disk/trash/resources/restore?path=%s".formatted(folderPath))
                .then()
                .log().all()
                .statusCode(401)
                .body("error", equalTo("UnauthorizedError"))
                .body("description", equalTo("Unauthorized"))
                .body("message", equalTo("Не авторизован."));
    }

    @Test
    public void testRestoreNonExistentFolder() {
        given()
                .spec(requestSpec)
                .put("v1/disk/trash/resources/restore?path=NonExistentResource")
                .then()
                .log().all()
                .statusCode(404)
                .body("error", equalTo("DiskNotFoundError"))
                .body("description", equalTo("Resource not found."))
                .body("message", equalTo("Не удалось найти запрошенный ресурс."));
    }
}
