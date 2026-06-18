package models.yandex.disk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopyFileResponse {

    private String href;
    private String method;
    private Boolean templated;
}
