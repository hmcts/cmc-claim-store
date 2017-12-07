package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinRequest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Component
public class UserService {

    private IdamApi idamApi;
    private final AuthTokenGenerator cachedServiceAuthTokenGenerator;

    @Autowired
    public UserService(final IdamApi idamApi,
                       final AuthTokenGenerator cachedServiceAuthTokenGenerator) {
        this.idamApi = idamApi;
        this.cachedServiceAuthTokenGenerator = cachedServiceAuthTokenGenerator;
    }

    public UserDetails getUserDetails(final String authorisation) {
        return idamApi.retrieveUserDetails(authorisation);
    }

    public GeneratePinResponse generatePin(final String name, final String authorisation) {
        return idamApi.generatePin(
            new GeneratePinRequest(name),
            authorisation.replace("Bearer: ", "")
        );
    }

    public String generateServiceAuthToken() {
        return cachedServiceAuthTokenGenerator.generate();
    }
}
