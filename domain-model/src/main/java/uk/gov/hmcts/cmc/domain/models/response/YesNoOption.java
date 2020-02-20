package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public enum YesNoOption {
    @JsonProperty("yes")
    YES,
    @JsonProperty("no")
    NO;

    public static YesNoOption fromValue(String value) {
        return Arrays.stream(YesNoOption.values())
            .filter(val -> val.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown YesNoOption: " + value));
    }
}
