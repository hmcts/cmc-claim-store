package uk.gov.hmcts.cmc.claimstore.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
}
