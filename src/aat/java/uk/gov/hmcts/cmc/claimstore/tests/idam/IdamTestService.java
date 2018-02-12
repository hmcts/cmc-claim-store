package uk.gov.hmcts.cmc.claimstore.tests.idam;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.tests.AATConfiguration;

import static java.lang.String.format;

@Service
public class IdamTestService {

    private final IdamTestApi idamTestApi;
    private final UserService userService;
    private final AATConfiguration aatConfiguration;

    @Autowired
    public IdamTestService(
        IdamTestApi idamTestApi,
        UserService userService,
        AATConfiguration aatConfiguration
    ) {
        this.idamTestApi = idamTestApi;
        this.userService = userService;
        this.aatConfiguration = aatConfiguration;
    }

    public User createDefendant() {
        String email = format(aatConfiguration.getTestUserEmailPattern(), RandomStringUtils.randomAlphanumeric(10));
        idamTestApi.createUser(createDefendantRequest(email, aatConfiguration.getTestUser().getPassword()));
        return userService.authenticateUser(email, aatConfiguration.getTestUser().getPassword());
    }

    private CreateUserRequest createDefendantRequest(String username, String password) {
        return new CreateUserRequest(
            username,
            new UserGroup("cmc-private-beta"),
            password
        );
    }

}
