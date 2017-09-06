package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.client.IdamClient;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinRequest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

@Component
public class UserService {

    private IdamClient idamClient;

    public UserService(final IdamClient idamClient) {
        this.idamClient = idamClient;
    }

    public UserDetails getUserDetails(final String authorisation) {
        return idamClient.retrieveUserDetails(authorisation);
    }

    public GeneratePinResponse generatePin(final String name, final String authorisation) {
        return idamClient.generatePin(
            new GeneratePinRequest(name),
            authorisation.replace("Bearer: ", "")
        );
    }
}
