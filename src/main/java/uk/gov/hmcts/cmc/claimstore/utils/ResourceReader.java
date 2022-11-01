package uk.gov.hmcts.cmc.claimstore.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class ResourceReader {

    private ResourceReader() {
        // Utility class, no instances
    }

    public static String readString(String resourcePath) {
        return new String(
            readBytes(resourcePath),
            StandardCharsets.UTF_8
        );
    }

    public static byte[] readBytes(String resourcePath) {
        try (InputStream inputStream = ResourceReader.class.getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Unable to read resource: " + resourcePath, e);
        }
    }

    public static Properties readResource(String resourcePath) {
        try (InputStream input = ResourceReader.class.getClassLoader().getResourceAsStream(resourcePath)) {

            Properties prop = new Properties();
            if (input == null) {
                log.error("The file name cannot be null, please ensure the file name is configured correctly");
                return null;
            }

            prop.load(input);
            return prop;
        } catch (IOException ioException) {
            log.error("Unable to read resource: {}", ioException.getMessage());
        }
        return null;
    }

    public static void writeResource(String resourcePath, Map<String, String> properties, String comments) {
        try (OutputStream output = new FileOutputStream(
            String.valueOf(ResourceReader.class.getClassLoader().getResourceAsStream(resourcePath)))) {
            Properties props = new Properties();

            var valueMap = properties.entrySet();
                for (Map.Entry<String,String> val: valueMap){
                    props.setProperty(val.getKey(),val.getValue());
                }

            props.store(output, comments);
            System.out.println("the value of the resource "+readResource("page-increments.properties")
                .getProperty("page.number"));
            System.out.println("the property value of the props"+props);

        } catch (IOException ioException) {
            log.error("Unable to write resource: {}", ioException.getMessage());
        }
    }
}
