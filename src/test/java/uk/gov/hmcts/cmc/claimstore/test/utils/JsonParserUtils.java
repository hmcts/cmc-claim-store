package uk.gov.hmcts.cmc.claimstore.test.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for mapping json data to classes and type references.
 */
@UtilityClass
public class JsonParserUtils {

    private static ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(JsonParserUtils.class);

    public <T> T fromJson(String jsonFile, TypeReference<T> type) {
        try {
            return objectMapper.readValue(jsonFile, type);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return null;
        }
    }

    public <T> T fromJson(String jsonFile, Class<T> type) {
        try {
            return objectMapper.readValue(jsonFile, type);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return null;
        }
    }
}
