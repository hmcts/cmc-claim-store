package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.CachedServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

@Configuration
@Lazy
public class ServiceTokenGeneratorConfiguration {

    @Bean
    public AuthTokenGenerator authTokenGenerator(
        @Value("${idam.s2s-auth.totp_secret}") final String secret,
        @Value("${idam.s2s-auth.microservice}") final String microService,
        @Value("${idam.s2s-auth.tokenTimeToLiveInSeconds:14400}") final int ttl,
        final ServiceAuthorisationApi serviceAuthorisationApi) {

        final AuthTokenGenerator serviceAuthTokenGenerator
            = new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);

        return new CachedServiceAuthTokenGenerator(serviceAuthTokenGenerator, ttl);
    }
}
