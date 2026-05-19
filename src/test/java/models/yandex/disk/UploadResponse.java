package models.yandex.disk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    @JsonProperty("operation_id")
    private String operationId;

    private String href;
    private String method;
    private Boolean templated;
}
