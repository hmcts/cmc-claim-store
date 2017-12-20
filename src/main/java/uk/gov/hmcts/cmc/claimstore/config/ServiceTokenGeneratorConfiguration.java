package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AutorefreshingJwtAuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.BearerTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

@Configuration
@Lazy
public class ServiceTokenGeneratorConfiguration {

    @Bean
    public AuthTokenGenerator authTokenGenerator(
        @Value("${idam.s2s-auth.totp_secret}") String secret,
        @Value("${idam.s2s-auth.microservice}") String microService,
        ServiceAuthorisationApi serviceAuthorisationApi) {

        AuthTokenGenerator serviceAuthTokenGenerator
            = new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);

        return new BearerTokenGenerator(
            new AutorefreshingJwtAuthTokenGenerator(serviceAuthTokenGenerator)
        );
    }

}
