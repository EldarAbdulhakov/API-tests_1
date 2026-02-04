package tests;

import io.restassured.RestAssured;
import models.yandex.disk.DownloadResponse;
import models.yandex.disk.UploadResponse;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.yandex.disk.BaseYandexDiscTest;
import utils.FileFactory;

import java.nio.file.Path;

public class DownloadFile_TestCase4 extends BaseYandexDiscTest {

    private static final String FOLDER_NAME = "sdet_data";
    private static final String PREFIX_FILE_NAME = "data";
    private static final String SUFFIX_FILE_NAME = ".txt";
    private static final String FILE_NAME = PREFIX_FILE_NAME + SUFFIX_FILE_NAME;
    private static final String CONTENT = "username=SDET\\npassword=secret_key";

    @Test
    public void downloadFileTest() {
        Path file = FileFactory.createFile(PREFIX_FILE_NAME, SUFFIX_FILE_NAME, CONTENT);

        createFolder(FOLDER_NAME);

        // Получить ссылку на загрузку файла
        UploadResponse uploadLinkResponse = RestAssured.given()
                .spec(requestSpec)
                .get(NESTED_UPLOAD_RESOURCE_PATH.formatted(FOLDER_NAME, FILE_NAME))
                .then()
                .extract().as(UploadResponse.class);

        // Загрузить файл в папку
        RestAssured.given()
                .spec(requestSpec)
                .body(file.toFile())
                .put(uploadLinkResponse.getHref());

        // Получить ссылку на скачивание
        DownloadResponse downloadResponse = RestAssured.given()
                .spec(requestSpec)
                .queryParam("path", FOLDER_NAME + "/" + FILE_NAME)
                .get(DOWNLOAD_RESOURCE_PATH)
                .then()
                .statusCode(200)
                .extract().as(DownloadResponse.class);

        Assert.assertTrue(downloadResponse.getHref().contains("https://downloader.disk.yandex.ru/disk/"));

        // Скачать файл
        String actualText = RestAssured.given()
                .urlEncodingEnabled(false)
                .get(downloadResponse.getHref())
                .then()
                .extract().asString();

        Assert.assertEquals(actualText, CONTENT);
    }
}
