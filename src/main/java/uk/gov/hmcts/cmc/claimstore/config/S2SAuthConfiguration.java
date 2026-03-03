package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.beans.factory.annotation.Value;
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
    public AuthTokenValidator authTokenValidator(
        @Value("${idam.s2s-auth.url}") String s2sUrl,
        ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return new ServiceAuthTokenValidator(serviceAuthorisationApi);
    }

}
