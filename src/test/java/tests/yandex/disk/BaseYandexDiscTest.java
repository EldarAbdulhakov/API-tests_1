package tests.yandex.disk;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import utils.PropertyProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class BaseYandexDiscTest {

    protected final String FOLDER_NAME = "folderName";
    protected final String NESTED_FOLDER = "nestedFolder";
    protected final PropertyProvider PROPS = PropertyProvider.getInstance();
    protected RequestSpecification requestSpec;
    protected RequestSpecification requestSpecWithoutAuth;
    protected List<String> createdResources;

    @BeforeClass
    public void setUp() {
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(PROPS.getProperty("yandex.url"))
                .addHeader("Authorization", "OAuth " + PROPS.getProperty("yandex.auth.token"))
                .log(LogDetail.ALL)
                .build();

        requestSpecWithoutAuth = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(PROPS.getProperty("yandex.url"))
                .log(LogDetail.ALL)
                .build();
    }

    @BeforeMethod
    protected void initTestContent() {
        createdResources = new ArrayList<>();
    }

    @AfterMethod
    public void cleaUp() {
        for (String folderName : createdResources) {
            deleteFolderPermanently(folderName);
            deleteFolderFromTrash(folderName);
        }
        createdResources.clear();
    }

    protected void createFolder(String folderName) {
        given()
                .spec(requestSpec)
                .put("v1/disk/resources?path=%s".formatted(folderName));

        registerCreatedResource(folderName);
    }

    protected void deleteFolderToTrash(String folderName) {
        given()
                .spec(requestSpec)
                .delete("v1/disk/resources?path=%s".formatted(folderName));
    }

    private void deleteFolderPermanently(String folderName) {
        given()
                .spec(requestSpec)
                .delete("v1/disk/resources?path=%s&permanently=true".formatted(folderName));
    }

    private void deleteFolderFromTrash(String resource) {
        List<Map<String, Object>> items = given()
                .spec(requestSpec)
                .get("v1/disk/trash/resources")
                .then()
                .extract().path("_embedded.items");

        String folderPath = items.stream()
                .filter(item -> resource.equals(item.get("name")))
                .map(item -> (String) item.get("path"))
                .findFirst()
                .orElse(null);

        given()
                .spec(requestSpec)
                .delete("v1/disk/trash/resources?path=%s".formatted(folderPath));
    }

    protected void registerCreatedResource(String resource) {
        createdResources.add(resource);
    }
}
