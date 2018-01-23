package uk.gov.hmcts.cmc.claimstore.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.InvalidApplicationException;

import java.io.IOException;

@Service
public class JsonMapper {
    private final Logger logger = LoggerFactory.getLogger(JsonMapper.class);

    private static final String SERIALISATION_ERROR_MESSAGE = "Failed to serialize '%s' to JSON";
    private static final String DESERIALISATION_ERROR_MESSAGE = "Failed to deserialize '%s' from JSON";

    private final ObjectMapper objectMapper;

    public JsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);

            throw new InvalidApplicationException(
                String.format(SERIALISATION_ERROR_MESSAGE, input.getClass().getSimpleName()), e
            );
        }
    }

    public <T> T fromJson(String value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new InvalidApplicationException(
                String.format(DESERIALISATION_ERROR_MESSAGE, clazz.getSimpleName()), e
            );
        }
    }

    public <T> T fromJson(String value, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (IOException e) {
            throw new InvalidApplicationException(
                String.format(DESERIALISATION_ERROR_MESSAGE, typeReference.getType()), e
            );
        }
    }
}
