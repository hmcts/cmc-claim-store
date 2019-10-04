package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class FeesAndPaymentsAreUp extends AllNestedConditions {

    public FeesAndPaymentsAreUp() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(prefix = "fees", name = "api.url")
    static class FeesIsUp {

    }

    @ConditionalOnProperty(prefix = "payments", name = "api.url")
    static class PaymentsIsUp {

    }

}
