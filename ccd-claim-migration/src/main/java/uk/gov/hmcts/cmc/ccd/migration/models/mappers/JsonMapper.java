package uk.gov.hmcts.cmc.ccd.migration.models.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JsonMapper {

    private static final String SERIALISATION_ERROR_MESSAGE = "Failed to serialize '%s' to JSON";
    private static final String DESERIALIZATION_ERROR_MESSAGE = "Failed to deserialize '%s' from JSON";

    private final ObjectMapper objectMapper;

    public JsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                String.format(SERIALISATION_ERROR_MESSAGE, input.getClass().getSimpleName()), e
            );
        }
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

    public <T> T fromJson(String value, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(
                String.format(DESERIALIZATION_ERROR_MESSAGE, typeReference.getType()), e
            );
        }
    }
}
