package uk.gov.hmcts.cmc.claimstore.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParagraphEnumerator {
    private ParagraphEnumerator() {
    }

    public static List<String> split(String body) {
        return Arrays.stream(body.split("[\\n\\r]+"))
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .collect(Collectors.toList());
    }
}
