package uk.gov.hmcts.cmc.claimstore.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ResourceReader {

    private ResourceReader() {
        // Utility class, no instances
    }

    public static String readString(String resourcePath) {
        return new String(
            readBytes(resourcePath),
            Charset.forName("UTF-8")
        );
    }

    public static byte[] readBytes(String resourcePath) {
        try (InputStream inputStream = ResourceReader.class.getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
