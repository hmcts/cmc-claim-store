package uk.gov.hmcts.cmc.claimstore.requests.courtfinder;

import com.fasterxml.jackson.annotation.JsonProperty;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;

public class CourtFinderOAuthInterceptor implements RequestInterceptor {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;
    private final String tenantId;
    private final String dataApiClientId;
    private final Clock clock;
    private String accessToken;
    private Instant refreshAt = Instant.MIN;

    public CourtFinderOAuthInterceptor(RestTemplate restTemplate, String clientId, String clientSecret,
                                     String tenantId, String dataApiClientId) {
        this(restTemplate, clientId, clientSecret, tenantId, dataApiClientId, Clock.systemUTC());
    }

    CourtFinderOAuthInterceptor(RestTemplate restTemplate, String clientId, String clientSecret,
                               String tenantId, String dataApiClientId, Clock clock) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tenantId = tenantId;
        this.dataApiClientId = dataApiClientId;
        this.clock = clock;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.removeHeader(HttpHeaders.AUTHORIZATION);
        template.header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken());
    }

    private synchronized String getAccessToken() {
        if (accessToken != null && clock.instant().isBefore(refreshAt)) {
            return accessToken;
        }
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)
            || !StringUtils.hasText(tenantId) || !StringUtils.hasText(dataApiClientId)) {
            throw new IllegalStateException("CourtFinder OAuth credentials are not configured");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "client_credentials");
        form.add("scope", "api://" + dataApiClientId + "/.default");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        final Instant requestedAt = clock.instant();
        TokenResponse response;
        try {
            response = restTemplate.postForObject(
                "https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token",
                new HttpEntity<>(form, headers), TokenResponse.class, tenantId);
        } catch (RestClientException exception) {
            // Do not expose the token endpoint response or credentials in application errors.
            throw new IllegalStateException("Unable to obtain CourtFinder OAuth token");
        }
        if (response == null || !StringUtils.hasText(response.accessToken) || response.expiresIn <= 0) {
            throw new IllegalStateException("Invalid CourtFinder OAuth token response");
        }
        accessToken = response.accessToken;
        refreshAt = requestedAt.plusSeconds(response.expiresIn - Math.min(60, response.expiresIn / 2));
        return accessToken;
    }

    public static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private long expiresIn;
    }
}
