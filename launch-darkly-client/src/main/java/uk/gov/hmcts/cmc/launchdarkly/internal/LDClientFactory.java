package uk.gov.hmcts.cmc.launchdarkly.internal;

import com.launchdarkly.client.LDClient;
import org.springframework.stereotype.Service;

@Service
public class LDClientFactory {
    public LDClient create(String sdkKey) {
        return new LDClient(sdkKey);
    }
}
