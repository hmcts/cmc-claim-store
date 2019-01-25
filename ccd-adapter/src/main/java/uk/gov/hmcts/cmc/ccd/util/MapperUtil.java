package uk.gov.hmcts.cmc.ccd.util;

import java.util.Objects;
import java.util.stream.Stream;

public class MapperUtil {

    private MapperUtil(){
        // Utility class, no instances
    }

    public static boolean isAllNull(Object... objects) {
        return Stream.of(objects).allMatch(Objects::nonNull);
    }

    public static boolean isAnyNotNull(Object... objects) {
        return Stream.of(objects).anyMatch(Objects::nonNull);
    }
}
