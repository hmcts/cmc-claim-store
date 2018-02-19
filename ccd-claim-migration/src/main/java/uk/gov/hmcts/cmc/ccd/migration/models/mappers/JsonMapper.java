package uk.gov.hmcts.cmc.ccd.migration.models.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JsonMapper {

    private static final String DESERIALIZATION_ERROR_MESSAGE = "Failed to deserialize '%s' from JSON";

    private final ObjectMapper objectMapper;

    public JsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T fromJson(String value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            throw new RuntimeException(
                String.format(DESERIALIZATION_ERROR_MESSAGE, clazz.getSimpleName()), e
            );
        }
    }
}
