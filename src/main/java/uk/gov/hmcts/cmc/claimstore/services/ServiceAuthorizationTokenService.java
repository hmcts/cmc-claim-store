package uk.gov.hmcts.cmc.claimstore.services;

import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

public class ServiceAuthorizationTokenService {
    private final AuthTokenGenerator cachedServiceAuthTokenGenerator;

    public ServiceAuthorizationTokenService(final AuthTokenGenerator cachedServiceAuthTokenGenerator) {
        this.cachedServiceAuthTokenGenerator = cachedServiceAuthTokenGenerator;
    }

    public String getToken() {
        return cachedServiceAuthTokenGenerator.generate();
    }
}
