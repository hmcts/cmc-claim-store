package uk.gov.hmcts.cmc.claimstore.healthcheck;

import com.launchdarkly.client.LDUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaunchDarklyHealthIndicatorTest {
    @Mock
    private LaunchDarklyClient client;

    private LaunchDarklyHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new LaunchDarklyHealthIndicator(client);
    }

    @Test
    void testToggleEnabled() {
        when(client.isFeatureEnabled(eq("launch-darkly-health-roc-8014"), any(LDUser.class))).thenReturn(true);
        Health result = healthIndicator.health();
        assertNotNull(result);
        assertEquals(Status.UP, result.getStatus());
        Map<String, Object> details = result.getDetails();
        assertTrue(details.containsKey("launch-darkly-health-roc-8014"));
        assertTrue((boolean) details.get("launch-darkly-health-roc-8014"));
    }

    @Test
    void testToggleDisabled() {
        when(client.isFeatureEnabled(eq("launch-darkly-health-roc-8014"), any(LDUser.class))).thenReturn(false);
        Health result = healthIndicator.health();
        assertNotNull(result);
        assertEquals(Status.UP, result.getStatus());
        Map<String, Object> details = result.getDetails();
        assertTrue(details.containsKey("launch-darkly-health-roc-8014"));
        assertFalse((boolean) details.get("launch-darkly-health-roc-8014"));
    }
}
