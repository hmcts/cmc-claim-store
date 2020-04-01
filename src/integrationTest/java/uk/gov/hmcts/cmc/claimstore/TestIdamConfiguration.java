package uk.gov.hmcts.cmc.claimstore;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.context.ContextCleanupListener;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TestIdamConfiguration extends ContextCleanupListener {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(clientRegistration());
    }

    private ClientRegistration clientRegistration() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("end_session_endpoint", "https://idam/logout");

        return ClientRegistration.withRegistrationId("oidc")
            .redirectUriTemplate("{baseUrl}/{action}/oauth2/code/{registrationId}")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .scope("read:user")
            .authorizationUri("http://idam/o/authorize")
            .tokenUri("http://idam/o/access_token")
            .userInfoUri("http://idam/o/userinfo")
            .jwkSetUri("http://idam/0/oauth/jwk")
            .providerConfigurationMetadata(metadata)
            .userNameAttributeName("id")
            .clientName("Client Name")
            .clientId("client-id")
            .clientSecret("client-secret")
            .build();
    }
}
