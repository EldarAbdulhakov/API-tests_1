package tests.yandex.disk;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import utils.PropertyProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class BaseYandexDiscTest {

    protected static final String RESOURCE_PATH = "v1/disk/resources?path=%s";
    protected static final String NESTED_RESOURCE_PATH = "v1/disk/resources?path=%s/%s";
    protected static final String RESOURCE_PATH_PERMANENTLY = "v1/disk/resources?path=%s&permanently=true";
    protected static final String UPLOAD_RESOURCE_PATH = "v1/disk/resources/upload?path=%s";
    protected static final String NESTED_UPLOAD_RESOURCE_PATH = "v1/disk/resources/upload?path=%s/%s";
    protected static final String TRASH_RESOURCES_PATH = "v1/disk/trash/resources";
    protected static final String RESTORE_FROM_TRASH_PATH = "v1/disk/trash/resources/restore?path=%s";
    protected static final String DOWNLOAD_RESOURCE_PATH = "v1/disk/resources/download";
    protected static final String COPY_RESOURCE_PATH = "v1/disk/resources/copy";

    protected ThreadLocal<String> uniqueFolderName = new ThreadLocal<>();
    protected static final String NESTED_FOLDER = "nestedFolder";
    protected ThreadLocal<String> uniqueFileName = new ThreadLocal<>();
    protected final PropertyProvider PROPS = PropertyProvider.getInstance();
    protected RequestSpecification requestSpec;
    protected RequestSpecification requestSpecWithoutAuth;
    protected ThreadLocal<List<String>> createdResources = ThreadLocal.withInitial(ArrayList::new);

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
    protected void initTestContent(Method method) {
        createdResources.get().clear();

        String unique = method.getName() + "_" + UUID.randomUUID();
        uniqueFolderName.set(unique);
        uniqueFileName.set(unique + ".txt");
    }

    @AfterMethod
    public void cleaUp() {
        for (String folderName : createdResources.get()) {
            deleteResourcePermanently(folderName);
            deleteResourceFromTrash(folderName);
        }
        createdResources.remove();
        uniqueFolderName.remove();
        uniqueFileName.remove();
    }

    protected void createFolder(String folderName) {
        given()
                .spec(requestSpec)
                .put("v1/disk/resources?path=%s".formatted(folderName));

        registerCreatedResource(folderName);
    }

    protected void deleteResourceToTrash(String folderName) {
        given()
                .spec(requestSpec)
                .delete("v1/disk/resources?path=%s".formatted(folderName));
    }

    private void deleteResourcePermanently(String folderName) {
        given()
                .spec(requestSpec)
                .delete("v1/disk/resources?path=%s&permanently=true".formatted(folderName));
    }

    private void deleteResourceFromTrash(String resource) {
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
        createdResources.get().add(resource);
    }

    public void createTextFileInRoot() {
        String fileName = UUID.randomUUID() + ".txt";

        String href = given()
                .spec(requestSpec)
                .get(UPLOAD_RESOURCE_PATH.formatted(fileName))
                .then()
                .extract().path("href");

        given()
                .spec(requestSpec)
                .put(href)
                .then();

        registerCreatedResource(fileName);
    }
}
