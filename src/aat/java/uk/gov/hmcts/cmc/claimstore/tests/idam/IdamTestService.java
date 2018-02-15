package uk.gov.hmcts.cmc.claimstore.tests.idam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.tests.AATConfiguration;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.TestData;

@Service
public class IdamTestService {

    private final IdamTestApi idamTestApi;
    private final UserService userService;
    private final TestData testData;
    private final AATConfiguration aatConfiguration;

    @Autowired
    public IdamTestService(
        IdamTestApi idamTestApi,
        UserService userService,
        TestData testData,
        AATConfiguration aatConfiguration
    ) {
        this.idamTestApi = idamTestApi;
        this.userService = userService;
        this.testData = testData;
        this.aatConfiguration = aatConfiguration;
    }

    public User createDefendant() {
        String email = testData.nextUserEmail();
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
