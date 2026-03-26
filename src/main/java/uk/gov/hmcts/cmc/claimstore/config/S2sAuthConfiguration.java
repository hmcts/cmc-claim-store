package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cmc.claimstore.security.S2sAuthFilter;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class S2sAuthConfiguration {

    @Bean
    public S2sAuthFilter s2sAuthFilter(
        ServiceAuthorisationApi serviceAuthorisationApi,
        @Value("${idam.s2s-auth.services-allowed:}") String allowedServices
    ) {
        List<String> services = Arrays.stream(allowedServices.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toList());

        return new S2sAuthFilter(new ServiceAuthTokenValidator(serviceAuthorisationApi), services);
    }
}
