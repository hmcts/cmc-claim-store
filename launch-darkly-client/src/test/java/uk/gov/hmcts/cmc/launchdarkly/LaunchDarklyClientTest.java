package uk.gov.hmcts.cmc.launchdarkly;

import com.launchdarkly.client.LDClientInterface;
import com.launchdarkly.client.LDUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.launchdarkly.internal.LDClientFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaunchDarklyClientTest {
    private static final String SDK_KEY = "fake key";
    private static final String FAKE_FEATURE = "fake feature";

    @Mock
    private LDClientFactory ldClientFactory;

    @Mock
    private LDClientInterface ldClient;

    @Mock
    private LDUser ldUser;

    private LaunchDarklyClient launchDarklyClient;

    @BeforeEach
    void setUp() {
        when(ldClientFactory.create(eq(SDK_KEY), anyBoolean())).thenReturn(ldClient);
        launchDarklyClient = new LaunchDarklyClient(ldClientFactory, SDK_KEY, true);
    }

    @Test
    void testFeatureEnabled() {
        when(ldClient.boolVariation(eq(FAKE_FEATURE), any(LDUser.class), anyBoolean())).thenReturn(true);
        assertTrue(launchDarklyClient.isFeatureEnabled(FAKE_FEATURE, ldUser));
    }

    @Test
    void testFeatureDisabled() {
        when(ldClient.boolVariation(eq(FAKE_FEATURE), any(LDUser.class), anyBoolean())).thenReturn(false);
        assertFalse(launchDarklyClient.isFeatureEnabled(FAKE_FEATURE, ldUser));
    }
}
