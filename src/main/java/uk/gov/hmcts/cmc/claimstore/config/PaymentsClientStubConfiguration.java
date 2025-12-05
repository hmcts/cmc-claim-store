package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.cmc.claimstore.services.payments.PaymentsClientStub;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;

@Configuration
@Profile("!prod")
public class PaymentsClientStubConfiguration {

    @Bean
    @Primary
    public PaymentsClient paymentsClientStub() {
        return new PaymentsClientStub();
    }
}
