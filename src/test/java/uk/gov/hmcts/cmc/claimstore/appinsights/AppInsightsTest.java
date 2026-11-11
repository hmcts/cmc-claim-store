package uk.gov.hmcts.cmc.claimstore.appinsights;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppInsightsTest {
    @Mock
    private TelemetryClient telemetryClient;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger = (Logger) LoggerFactory.getLogger(AppInsights.class);

    private AppInsights appInsights;

    @BeforeEach
    void prepareLogger() {
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void detachLogger() {
        logger.detachAppender(listAppender);
    }

    @Nested
    @DisplayName("With active telemetry client")
    class ActiveTelemetry {
        @BeforeEach
        void setUp() {
            appInsights = new AppInsights(telemetryClient, "");
        }

        @Test
        void testTrackEvent() {
            appInsights.trackEvent(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE, "reference", "value");
            verify(telemetryClient).trackEvent(
                eq(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE.toString()),
                eq(singletonMap("reference", "value")),
                isNull()
            );
        }

        @Test
        void testTrackException() {
            RuntimeException exception = new RuntimeException("expected exception");
            appInsights.trackException(exception);
            verify(telemetryClient).trackException(exception);
        }

        @AfterEach
        void verifyLoggerNotUsed() {
            List<ILoggingEvent> logsList = listAppender.list;
            assertThat(logsList).isEmpty();
        }
    }

    @Nested
    @DisplayName("With STDOUT")
    class StdoutTelemetry {

        @BeforeEach
        void setUp() {
            appInsights = new AppInsights(telemetryClient, "STDOUT");
        }

        @Test
        void testTrackEventSingleReference() {
            appInsights.trackEvent(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE, "reference", "value");
            verify(telemetryClient).trackEvent(
                eq(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE.toString()),
                eq(singletonMap("reference", "value")),
                isNull()
            );

            List<ILoggingEvent> logsList = listAppender.list;
            assertThat(logsList).hasSize(1);
            ILoggingEvent log = logsList.get(0);
            assertAll(
                () -> assertThat(log.getLevel())
                    .isEqualTo(Level.INFO),
                () -> assertThat(log.getFormattedMessage())
                    .isEqualTo("AppInsights event: Claim attempt - Duplicate: {reference=value}")
            );
        }

        @Test
        void testTrackEventMultipleReferences() {
            ImmutableMap<String, String> properties = ImmutableMap.of(
                "reference1", "value1",
                "reference2", "value2"
            );
            appInsights.trackEvent(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE, properties);
            verify(telemetryClient).trackEvent(
                eq(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE.toString()),
                eq(properties),
                isNull()
            );

            List<ILoggingEvent> logsList = listAppender.list;
            assertThat(logsList).hasSize(1);
            ILoggingEvent log = logsList.get(0);
            assertAll(
                () -> assertThat(log.getLevel())
                    .isEqualTo(Level.INFO),
                () -> assertThat(log.getFormattedMessage())
                    .isEqualTo("AppInsights event: Claim attempt - Duplicate: "
                        + "{reference1=value1, reference2=value2}")
            );
        }

        @Test
        void testTrackException() {
            RuntimeException exception = new RuntimeException("expected exception");
            appInsights.trackException(exception);
            verify(telemetryClient).trackException(exception);

            List<ILoggingEvent> logsList = listAppender.list;
            assertThat(logsList).hasSize(1);
            ILoggingEvent log = logsList.get(0);
            assertAll(
                () -> assertThat(log.getLevel())
                    .isEqualTo(Level.WARN),
                () -> assertThat(log.getFormattedMessage())
                    .isEqualTo("AppInsights exception: RuntimeException"),
                () -> assertThat(log.getThrowableProxy())
                    .extracting(IThrowableProxy::getClassName)
                    .isEqualTo("java.lang.RuntimeException")
            );
        }
    }
}
