package uk.gov.hmcts.cmc.launchdarkly.internal;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import com.launchdarkly.sdk.server.LDConfig;
import org.springframework.stereotype.Service;

@Service
public class LDClientFactory {
    public LDClientInterface create(String sdkKey, boolean offlineMode) {
        LDConfig config = new LDConfig.Builder()
            .offline(offlineMode)
            .build();
        return new LDClient(sdkKey, config);
    }
}
