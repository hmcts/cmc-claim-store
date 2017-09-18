package uk.gov.hmcts.cmc.claimstore.utils;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Optionals {
    private Optionals() {
        //utility class
    }

    public static <T> Stream<T> toStream(final Optional<T> opt) {
        return opt.map(Stream::of).orElse(Stream.empty());
    }

    public static boolean isPresent(final Object object) {
        return Optional.ofNullable(object).isPresent();
    }

    public static boolean isAbsent(final Object object) {
        return !isPresent(object);
    }

    public static boolean areEqual(final Object o1, final Object o2) {
        return Optional.ofNullable(o1).map(o -> o.equals(o2)).orElse(false);
    }

    public static Optional<String> stringToOptional(final String string) {
        return string.isEmpty() ? Optional.empty() : Optional.of(string);
    }
}
