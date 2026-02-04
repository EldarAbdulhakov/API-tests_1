package tests;

import io.restassured.RestAssured;
import models.yandex.disk.CopyFileResponse;
import models.yandex.disk.ErrorResponse;
import models.yandex.disk.UploadResponse;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.yandex.disk.BaseYandexDiscTest;
import utils.FileFactory;

import java.nio.file.Path;

public class UploadAndCopyFile_TestCase3 extends BaseYandexDiscTest {

    private static final String INPUT_FOLDER_NAME = "input_data";
    private static final String OUTPUT_FOLDER_NAME = "output_data";
    private static final String PREFIX_FILE_NAME = "data";
    private static final String SUFFIX_FILE_NAME = ".txt";
    private static final String FILE_NAME = PREFIX_FILE_NAME + SUFFIX_FILE_NAME;
    private static final String CONTENT = "username=SDET\\npassword=secret_key";

    @Test
    public void uploadAndCopyFileTest() {
        createFolder(INPUT_FOLDER_NAME);
        createFolder(OUTPUT_FOLDER_NAME);

        Path file = FileFactory.createFile(PREFIX_FILE_NAME, SUFFIX_FILE_NAME, CONTENT);

        // Получить ссылку на загрузку файла
        UploadResponse uploadResponse = RestAssured.given()
                .spec(requestSpec)
                .get(NESTED_UPLOAD_RESOURCE_PATH.formatted(INPUT_FOLDER_NAME, FILE_NAME))
                .then()
                .extract().as(UploadResponse.class);

        // Загрузить файл в папку
        RestAssured.given()
                .spec(requestSpec)
                .body(file.toFile())
                .put(uploadResponse.getHref())
                .then()
                .statusCode(201);

        // Копировать файл
        CopyFileResponse copyFileResponse = RestAssured.given()
                .spec(requestSpec)
                .queryParam("from", INPUT_FOLDER_NAME + "/" + FILE_NAME)
                .queryParam("path", OUTPUT_FOLDER_NAME + "/" + FILE_NAME)
                .post(COPY_RESOURCE_PATH)
                .then()
                .statusCode(201)
                .extract().as(CopyFileResponse.class);

        Assert.assertNotNull(copyFileResponse.getHref());
        Assert.assertNotNull(copyFileResponse.getMethod());
        Assert.assertNotNull(copyFileResponse.getTemplated());

        // Повторить копирование файла
        ErrorResponse errorCopyFileResponse = RestAssured.given()
                .spec(requestSpec)
                .queryParam("from", INPUT_FOLDER_NAME + "/" + FILE_NAME)
                .queryParam("path", OUTPUT_FOLDER_NAME + "/" + FILE_NAME)
                .post(COPY_RESOURCE_PATH)
                .then()
                .statusCode(409)
                .extract().as(ErrorResponse.class);

        Assert.assertNotNull(errorCopyFileResponse.getError());
        Assert.assertNotNull(errorCopyFileResponse.getDescription());
        Assert.assertNotNull(errorCopyFileResponse.getMessage());
    }
}
