package uk.gov.hmcts.cmc.claimstore.idam.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Oauth2 {

    private final String clientId;
    private final String redirectUrl;
    private final String clientSecret;

    @Autowired
    public Oauth2(
        @Value("${frontend.base.url}") String baseUrl,
        @Value("${oauth2.client.id}") String clientId,
        @Value("${oauth2.client.secret}") String clientSecret
    ) {
        this.clientId = clientId;
        this.redirectUrl = baseUrl + "/receiver";
        this.clientSecret = clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
