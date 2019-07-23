package uk.gov.hmcts.cmc.ccd.adapter.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

public class StreamUtil {

    private StreamUtil() {
        // Utility class, no instances
    }

    public static <T> Stream<T> asStream(final Collection<T> collection) {
        return Optional.ofNullable(collection)
            .orElse(Collections.emptySet()).stream();
    }
}
