package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinRequest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

@Component
public class UserService {

    private IdamApi idamApi;

    @Autowired
    public UserService(IdamApi idamApi) {
        this.idamApi = idamApi;
    }

    public UserDetails getUserDetails(String authorisation) {
        return idamApi.retrieveUserDetails(authorisation);
    }

    public User getUser(String authorisation) {
        return new User(authorisation, idamApi.retrieveUserDetails(authorisation));
    }

    public User authenticateAnonymousCaseWorker() {
        return new User("jkljasd", new UserDetails("1", "1@1.com", "1", "123"));
    }

    public GeneratePinResponse generatePin(String name, String authorisation) {
        return idamApi.generatePin(
            new GeneratePinRequest(name),
            authorisation.replace("Bearer: ", "")
        );
    }
}
