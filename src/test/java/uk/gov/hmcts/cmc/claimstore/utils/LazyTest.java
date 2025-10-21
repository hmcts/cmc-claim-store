package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class LazyTest {
    private AtomicInteger invocationCount;

    private Lazy<Integer> lazy;

    @BeforeEach
    void setUp() {
        invocationCount = new AtomicInteger();
        lazy = Lazy.lazily(invocationCount::incrementAndGet);
    }

    @Test
    @DisplayName("a new Lazy should not be invoked")
    void shouldNotBeInvoked() {
        assertThat(invocationCount).hasValue(0);
    }

    @Nested
    @DisplayName("when it is called")
    class InvokeOnce {
        @BeforeEach
        void invokeOnce() {
            assertThat(lazy.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("the supplier should be invoked once")
        void shouldBeInvokedOnce() {
            assertThat(invocationCount).hasValue(1);
        }

        @Test
        @DisplayName("calling it again should not repeat the supplier invocation")
        void repeatedInvocationShouldNotRunSupplier() {
            assertAll(
                () -> assertThat(lazy.get()).isEqualTo(1),
                () -> assertThat(invocationCount).hasValue(1)
            );
        }
    }
}
