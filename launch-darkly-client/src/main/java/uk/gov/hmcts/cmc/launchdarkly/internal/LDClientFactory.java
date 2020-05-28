package uk.gov.hmcts.cmc.launchdarkly.internal;

import com.launchdarkly.client.LDClient;
import com.launchdarkly.client.LDClientInterface;
import com.launchdarkly.client.LDConfig;
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
