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
class JsonParserUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JsonParserUtils.class);

    /**
     * Util function to map the contents of a JSON file
     * to generic Type Reference.
     *
     * @param jsonFileSrc for a provide json string value
     * @param type        for a provided generic type reference
     * @return {@linkplain T}
     */
    static <T> T fromJson(String jsonFileSrc, TypeReference<T> type) {
        try {
            String jsonValue = FileReaderUtils.readJsonFromFile(jsonFileSrc);
            return objectMapper.readValue(jsonValue, type);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return null;
        }
    }

    /**
     * Util function to map the contents of a JSON file
     * to generic Class Reference.
     *
     * @param jsonFileSrc for a provide json string value
     * @param type        for a provided generic class
     * @return {@linkplain T}
     */
    static <T> T fromJson(String jsonFileSrc, Class<T> type) {
        try {
            String jsonValue = FileReaderUtils.readJsonFromFile(jsonFileSrc);
            return objectMapper.readValue(jsonValue, type);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return null;
        }
    }
}
