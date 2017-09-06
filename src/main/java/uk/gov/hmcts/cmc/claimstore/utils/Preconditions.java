package uk.gov.hmcts.cmc.claimstore.utils;

import static java.util.Objects.requireNonNull;

public class Preconditions {

    private Preconditions() {
        // Statics utility class, no instances
    }

    public static String requireNonBlank(String string) {
        requireNonNull(string);
        if (string.isEmpty()) {
            throw new IllegalArgumentException("String argument cannot be empty");
        }
        return string;
    }

}
