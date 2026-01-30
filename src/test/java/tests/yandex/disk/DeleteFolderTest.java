package tests.yandex.disk;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class DeleteFolderTest extends BaseYandexDiscTest {

    @Test
    public void testDeleteFolderToTrash() {
        createFolder(FOLDER_NAME);

        given()
                .spec(requestSpec)
                .delete("v1/disk/resources?path=%s".formatted(FOLDER_NAME))
                .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void testDeleteFolderPermanently() {
        createFolder(FOLDER_NAME);

        given()
                .spec(requestSpec)
                .delete("v1/disk/resources?path=%s&permanently=true".formatted(FOLDER_NAME))
                .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void testDeleteFolderWithNestedFolderToTrash() {
        createFolder(FOLDER_NAME);
        createFolder(FOLDER_NAME + "/" + NESTED_FOLDER);

        given()
                .spec(requestSpec)
                .delete("v1/disk/resources?path=%s&permanently=true".formatted(FOLDER_NAME))
                .then()
                .log().all()
                .statusCode(202)
                .body("method", equalTo("GET"))
                .body("href", containsString("https://cloud-api.yandex.net/v1/disk/operations/"))
                .body("templated", equalTo(false));
    }

    @Test
    public void testDeleteNonExistentFolderToTrash() {
        given()
                .spec(requestSpec)
                .delete("v1/disk/resources?path=%s".formatted(FOLDER_NAME))
                .then()
                .log().all()
                .statusCode(404)
                .body("error", equalTo("DiskNotFoundError"))
                .body("description", equalTo("Resource not found."))
                .body("message", equalTo("Не удалось найти запрошенный ресурс."));
    }

    @Test
    public void testDeleteFolderWithoutOAuthToken() {
        createFolder(FOLDER_NAME);

        given()
                .spec(requestSpecWithoutAuth)
                .delete("v1/disk/resources?path=%s".formatted(FOLDER_NAME))
                .then()
                .log().all()
                .statusCode(401)
                .body("error", equalTo("UnauthorizedError"))
                .body("description", equalTo("Unauthorized"))
                .body("message", equalTo("Не авторизован."));
    }
}
