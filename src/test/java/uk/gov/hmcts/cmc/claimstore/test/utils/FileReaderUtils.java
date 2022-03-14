package uk.gov.hmcts.cmc.claimstore.test.utils;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A utility class that aids in the reading of file contents.
 */
@UtilityClass
class FileReaderUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileReaderUtils.class);

    /**
     * Reads the contents of a JSON file and stores it as a string.
     *
     * @param jsonFile for a provided json file
     * @return {@linkplain String} json file contents as string
     */
    static String readJsonFromFile(String jsonFile) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader =
                new BufferedReader(
                    new InputStreamReader(
                        new ClassPathResource(jsonFile).getInputStream()
                    )
                );

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            logger.error(String.valueOf(e));
        }

        return stringBuilder.toString();
    }
}
