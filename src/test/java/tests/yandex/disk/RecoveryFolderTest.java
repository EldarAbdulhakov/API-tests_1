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
        String folderName = uniqueFolderName.get();
        createFolder(folderName);
        deleteFolderToTrash(folderName);

        List<Map<String, Object>> items = given()
                .spec(requestSpec)
                .get(TRASH_RESOURCES_PATH)
                .then()
                .extract().path("_embedded.items");

        String folderPath = items.stream()
                .filter(item -> folderName.equals(item.get("name")))
                .map(item -> (String) item.get("path"))
                .findFirst()
                .orElse(null);

        Response response = given()
                .spec(requestSpec)
                .put(RESTORE_FROM_TRASH_PATH.formatted(folderPath))
                .then()
                .log().all()
                .extract().response();

        int statusCode = response.statusCode();

        if (statusCode == 201) {
            response.then()
                    .body("method", equalTo("GET"))
                    .body("href", equalTo("https://cloud-api.yandex.net/v1/disk/resources?path=disk%%3A%%2F%s".formatted(folderName)))
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
        String folderName = uniqueFolderName.get();
        createFolder(folderName);
        createFolder(folderName + "/" + NESTED_FOLDER);
        deleteFolderToTrash(folderName);

        List<Map<String, Object>> items = given()
                .spec(requestSpec)
                .get(TRASH_RESOURCES_PATH)
                .then()
                .extract().path("_embedded.items");

        String folderPath = items.stream()
                .filter(item -> folderName.equals(item.get("name")))
                .map(item -> (String) item.get("path"))
                .findFirst()
                .orElse(null);

        Response response = given()
                .spec(requestSpec)
                .put(RESTORE_FROM_TRASH_PATH.formatted(folderPath))
                .then()
                .log().all()
                .extract().response();

        int statusCode = response.statusCode();

        if (statusCode == 201) {
            response.then()
                    .body("method", equalTo("GET"))
                    .body("href", equalTo("https://cloud-api.yandex.net/v1/disk/resources?path=disk%%3A%%2F%s".formatted(folderName)))
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
        String folderName = uniqueFolderName.get();
        createFolder(folderName);
        deleteFolderToTrash(folderName);

        List<Map<String, Object>> items = given()
                .spec(requestSpec)
                .get(TRASH_RESOURCES_PATH)
                .then()
                .extract().path("_embedded.items");

        String folderPath = items.stream()
                .filter(item -> folderName.equals(item.get("name")))
                .map(item -> (String) item.get("path"))
                .findFirst()
                .orElse(null);

        given()
                .spec(requestSpecWithoutAuth)
                .put(RESTORE_FROM_TRASH_PATH.formatted(folderPath))
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
                .put(RESTORE_FROM_TRASH_PATH.formatted("NonExistentResource"))
                .then()
                .log().all()
                .statusCode(404)
                .body("error", equalTo("DiskNotFoundError"))
                .body("description", equalTo("Resource not found."))
                .body("message", equalTo("Не удалось найти запрошенный ресурс."));
    }
}
