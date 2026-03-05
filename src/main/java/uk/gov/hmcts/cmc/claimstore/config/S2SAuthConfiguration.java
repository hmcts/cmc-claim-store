package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

/**
 * Configuration for inbound S2S token validation.
 * This is separate from ServiceTokenGeneratorConfiguration which handles outbound token generation.
 */
@Configuration
public class S2SAuthConfiguration {

    @Bean
    @ConditionalOnProperty(name = "idam.s2s-auth.url")
    public AuthTokenValidator authTokenValidator(
        ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return new ServiceAuthTokenValidator(serviceAuthorisationApi);
    }

}
