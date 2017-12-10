package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.CachedServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

@Configuration
@Lazy
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class ServiceTokenGeneratorConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "authTokenGenerator")
    public AuthTokenGenerator authTokenGenerator(
            @Value("${idam.s2s-auth.totp_secret}") final String secret,
            @Value("${idam.s2s-auth.microservice}") final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cachedServiceAuthTokenGenerator")
    public AuthTokenGenerator cachedServiceAuthTokenGenerator(
            @Qualifier("authTokenGenerator") final AuthTokenGenerator serviceAuthTokenGenerator,
            @Value("${idam.s2s-auth.tokenTimeToLiveInSeconds:14400}") final int ttl) {
        return new CachedServiceAuthTokenGenerator(serviceAuthTokenGenerator, ttl);
    }
}
