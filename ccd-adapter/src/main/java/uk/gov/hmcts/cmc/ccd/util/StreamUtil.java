package uk.gov.hmcts.cmc.ccd.util;

import java.util.Collection;
import java.util.stream.Stream;

public class StreamUtil {

    private StreamUtil() {
        // Utility class, no instances
    }

    public static <T> Stream<T> asStream(final Collection<T> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }
}
