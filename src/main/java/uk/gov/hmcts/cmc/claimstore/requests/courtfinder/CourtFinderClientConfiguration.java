package uk.gov.hmcts.cmc.claimstore.requests.courtfinder;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

// Registered only in the CourtFinder Feign context to avoid authenticating other clients.
public class CourtFinderClientConfiguration {

    @Bean
    public RequestInterceptor courtFinderAuthentication(
        RestTemplate restTemplate,
        @Value("${courtfinder.api.client_id}") String clientId,
        @Value("${courtfinder.api.client_secret}") String clientSecret,
        @Value("${courtfinder.api.tenant_id}") String tenantId,
        @Value("${courtfinder.api.fact_data_api_id}") String dataApiClientId
    ) {
        return new CourtFinderOAuthInterceptor(restTemplate, clientId, clientSecret, tenantId, dataApiClientId);
    }
}
