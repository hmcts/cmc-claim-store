package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class FeesAndPaymentsConfiguration extends AllNestedConditions {

    public FeesAndPaymentsConfiguration() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(prefix = "fees", name = "api.url")
    static class FeesIsConfigured {

    }

    @ConditionalOnProperty(prefix = "payments", name = "api.url")
    static class PaymentsIsConfigured {

    }

}
