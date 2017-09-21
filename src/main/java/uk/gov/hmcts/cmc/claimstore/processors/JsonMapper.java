package uk.gov.hmcts.cmc.claimstore.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.InvalidApplicationException;

import java.io.IOException;

@Service
public class JsonMapper {
    private static final String SERIALISATION_ERROR_MESSAGE = "Failed to serialize '%s' to JSON";
    private static final String DESERIALISATION_ERROR_MESSAGE = "Failed to deserialize '%s' from JSON";

    private ObjectMapper objectMapper;

    public JsonMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(final Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new InvalidApplicationException(
                String.format(SERIALISATION_ERROR_MESSAGE, input.getClass().getSimpleName()), e
            );
        }
    }

    public <T> T fromJson(final String value, final Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            throw new InvalidApplicationException(
                String.format(DESERIALISATION_ERROR_MESSAGE, clazz.getSimpleName()), e
            );
        }
    }

    public <T> T fromJson(final String value, final TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (IOException e) {
            throw new InvalidApplicationException(
                String.format(DESERIALISATION_ERROR_MESSAGE, typeReference.getType()), e
            );
        }
    }
}
