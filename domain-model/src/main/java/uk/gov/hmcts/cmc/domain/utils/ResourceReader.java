package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.exceptions.FileNotFoundWithLinkException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ResourceReader {

    public String read(String input) {
        try {
            URL resource = getClass().getResource(input);
            URI url = resource.toURI();
            return new String(Files.readAllBytes(Paths.get(url)), UTF_8);
        } catch (NoSuchFileException e) {
            throw new FileNotFoundWithLinkException("no file found with the link '" + input + "'", e);
        } catch (IOException | URISyntaxException e) {
            throw new FileNotFoundWithLinkException("failed to read from file '" + input + "'", e);
        }
    }

}
