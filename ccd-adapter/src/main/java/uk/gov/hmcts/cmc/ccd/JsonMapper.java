package uk.gov.hmcts.cmc.ccd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;

import java.io.IOException;
import java.util.Map;

@Service("ccdJsonMapper")
public class JsonMapper {

    private static final String SERIALISATION_ERROR_MESSAGE = "Failed to serialize '%s' to JSON";
    private static final String DESERIALIZATION_ERROR_MESSAGE = "Failed to deserialize '%s' from JSON";

    private final ObjectMapper objectMapper;

    @Autowired
    public JsonMapper(ObjectMapper ccdObjectMapper) {
        this.objectMapper = ccdObjectMapper;
    }

    public String toJson(Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new MappingException(
                String.format(SERIALISATION_ERROR_MESSAGE, input.getClass().getSimpleName()), e
            );
        }
    }

    public <T> T fromMap(Map<String, Object> input, Class<T> clazz) {
        return objectMapper.convertValue(input, clazz);
    }

    public <T> T fromJson(String value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            throw new MappingException(
                String.format(DESERIALIZATION_ERROR_MESSAGE, clazz.getSimpleName()), e
            );
        }
    }

    public <T> T fromJson(String value, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (IOException e) {
            throw new MappingException(
                String.format(DESERIALIZATION_ERROR_MESSAGE, typeReference.getType()), e
            );
        }
    }

    public <T> T convertValue(Object value, Class<T> clazz) {
        return objectMapper.convertValue(value, clazz);
    }
}
