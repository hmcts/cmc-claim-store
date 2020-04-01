package uk.gov.hmcts.cmc.claimstore.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static String readFile(String filename) throws IOException {
        Path path = Paths.get(filename);

        return new String(Files.readAllBytes(path));
    }

    public static void writeFile(String filename, String contents) throws IOException {
        Path pathActual = Paths.get(filename);

        Files.write(pathActual, contents.getBytes());
    }
}
