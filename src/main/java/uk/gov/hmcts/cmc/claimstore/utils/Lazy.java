package uk.gov.hmcts.cmc.claimstore.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Lazy<T> {
    private final Supplier<T> supplier;
    private T instance;
    private boolean supplied = false;

    public static <T> Lazy<T> lazily(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public T get() {
        if (!supplied) {
            instance = supplier.get();
            supplied = true;
        }
        return instance;
    }
}
