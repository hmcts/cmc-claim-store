package uk.gov.hmcts.cmc.ccd.migration.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.exceptions.MigrationModelMappingException;

import java.io.IOException;
import java.util.Map;

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
            throw new MigrationModelMappingException(
                String.format(DESERIALIZATION_ERROR_MESSAGE, clazz.getSimpleName()), e
            );
        }
    }

    public <T> T fromJson(String value, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (IOException e) {
            throw new MigrationModelMappingException(
                String.format(DESERIALIZATION_ERROR_MESSAGE, typeReference.getType().getTypeName()), e
            );
        }
    }

    public <T> T fromMap(Map<String, Object> input, Class<T> clazz) {
        return objectMapper.convertValue(input, clazz);
    }
}
