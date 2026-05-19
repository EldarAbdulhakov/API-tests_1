package tests.yandex.disk;

import io.restassured.RestAssured;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetFilesTest extends BaseYandexDiscTest {

    @BeforeMethod
    public void createTwoFiles() {
        createTextFileInRoot();
        createTextFileInRoot();
    }

    @Test
    public void getFilesAndValidateSchemaTest() {
        RestAssured.given()
                .spec(requestSpec)
                .get("v1/disk/resources/files")
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("files-schema.json"));
    }
}
