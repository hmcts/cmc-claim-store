package uk.gov.hmcts.cmc.domain.models;

import java.util.Arrays;
import java.util.List;

public enum ScannedDocumentType {
    CHERISHED,
    OTHER,
    FORM,
    COVERSHEET;

    private final List<String> values;

    ScannedDocumentType(String... values) {
        this.values = Arrays.asList(values);
    }

    public List<String> getValues() {
        return values;
    }

    public static ScannedDocumentType fromValue(String value) {
        return Arrays.stream(values())
            .filter(v -> v.values.contains(value) || v.name().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown Claim Document Type: " + value));
    }
}
