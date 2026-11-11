package uk.gov.hmcts.cmc.claimstore.appinsights;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppInsightsTest {
    @Mock
    private io.opentelemetry.api.logs.Logger telemetry;

    @Mock
    private LogRecordBuilder logRecord;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger = (Logger) LoggerFactory.getLogger(AppInsights.class);

    private AppInsights appInsights;

    @BeforeEach
    void prepareLogger() {
        when(telemetry.logRecordBuilder()).thenReturn(logRecord);
        when(logRecord.setSeverity(any(Severity.class))).thenReturn(logRecord);
        when(logRecord.setBody(anyString())).thenReturn(logRecord);
        when(logRecord.setAllAttributes(any(Attributes.class))).thenReturn(logRecord);
        when(logRecord.setException(any(Throwable.class))).thenReturn(logRecord);
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
            appInsights = new AppInsights(telemetry, "");
        }

        @Test
        void testTrackEvent() {
            appInsights.trackEvent(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE, "reference", "value");
            verify(logRecord).setBody(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE.toString());
            verify(logRecord).setAllAttributes(Attributes.builder()
                .put("reference", "value")
                .build());
            verify(logRecord).emit();
        }

        @Test
        void testTrackException() {
            RuntimeException exception = new RuntimeException("expected exception");
            appInsights.trackException(exception);
            verify(logRecord).setBody("RuntimeException");
            verify(logRecord).setException(exception);
            verify(logRecord).emit();
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
            appInsights = new AppInsights(telemetry, "STDOUT");
        }

        @Test
        void testTrackEventSingleReference() {
            appInsights.trackEvent(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE, "reference", "value");
            verify(logRecord).setBody(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE.toString());
            verify(logRecord).setAllAttributes(Attributes.builder()
                .put("reference", "value")
                .build());
            verify(logRecord).emit();

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
            verify(logRecord).setBody(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE.toString());
            verify(logRecord).setAllAttributes(Attributes.builder()
                .put("reference1", "value1")
                .put("reference2", "value2")
                .build());
            verify(logRecord).emit();

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
            verify(logRecord).setBody("RuntimeException");
            verify(logRecord).setException(exception);
            verify(logRecord).emit();

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
