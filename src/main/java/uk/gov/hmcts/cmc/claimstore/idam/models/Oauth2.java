package uk.gov.hmcts.cmc.claimstore.idam.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Oauth2 {

    private String baseUrl;
    private String clientId;
    private String clientSecret;

    @Autowired
    public Oauth2(@Value("${frontend.base.url}") String baseUrl,
                  @Value("${oauth2.client.id:cmc_citizen}") String clientId,
                  @Value("${oauth2.s2s.top.secret}") String clientSecret) {
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getRedirectUrl() {
        return baseUrl + "/receiver";
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
