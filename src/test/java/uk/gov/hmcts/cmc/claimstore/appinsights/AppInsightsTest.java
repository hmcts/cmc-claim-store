package uk.gov.hmcts.cmc.claimstore.appinsights;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AppInsightsTest {
    @Mock
    private TelemetryClient telemetryClient;

    private AppInsights appInsights;

    @Before
    public void setUp() {
        appInsights = new AppInsights(telemetryClient);
    }

    @Test
    public void testTrackEvent() {
        appInsights.trackEvent(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE, "reference", "value");
        verify(telemetryClient).trackEvent(
            eq(AppInsightsEvent.CLAIM_ATTEMPT_DUPLICATE.toString()),
            eq(singletonMap("reference", "value")),
            isNull()
        );
    }

    @Test
    public void testTrackException() {
        RuntimeException exception = new RuntimeException("expected exception");
        appInsights.trackException(exception);
        verify(telemetryClient).trackException(exception);
    }
}
