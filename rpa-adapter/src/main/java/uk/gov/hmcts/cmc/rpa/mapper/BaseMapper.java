package uk.gov.hmcts.cmc.rpa.mapper;

import java.util.Optional;
import java.util.function.Function;

public class BaseMapper {
    protected <G, S extends G> String extractFromSubclass(G instance, Class<S> subclass, Function<S, String> mapper) {
        if (subclass.isInstance(instance)) {
            return mapper.apply((S) instance);
        } else {
            return null;
        }
    }

    protected <G, S extends G> String extractOptionalFromSubclass(G instance, Class<S> subclass,
                                                                  Function<S, Optional<String>> mapper) {
        return extractFromSubclass(instance, subclass, value -> mapper.apply((S) instance).orElse(null));
    }
}
