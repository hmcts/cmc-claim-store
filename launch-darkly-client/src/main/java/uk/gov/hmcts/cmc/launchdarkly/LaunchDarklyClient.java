package uk.gov.hmcts.cmc.launchdarkly;

import com.launchdarkly.client.LDClientInterface;
import com.launchdarkly.client.LDUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.launchdarkly.internal.LDClientFactory;

import java.io.IOException;

@Service
public class LaunchDarklyClient {
    public static final LDUser CLAIM_STORE_USER = new LDUser.Builder("claim-store-api")
        .anonymous(true)
        .build();

    private final LDClientInterface internalClient;

    @Autowired
    public LaunchDarklyClient(
        LDClientFactory ldClientFactory,
        @Value("${launchdarkly.sdk-key}") String sdkKey,
        @Value("${launchdarkly.offline-mode:false}") Boolean offlineMode
    ) {
        this.internalClient = ldClientFactory.create(sdkKey, offlineMode);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, LaunchDarklyClient.CLAIM_STORE_USER, false);
    }

    public boolean isFeatureEnabled(String feature, LDUser user) {
        return internalClient.boolVariation(feature, user, false);
    }

    private void close() {
        try {
            internalClient.close();
        } catch (IOException e) {
            // can't do anything clever here because things are being destroyed
            e.printStackTrace(System.err);
        }
    }
}
