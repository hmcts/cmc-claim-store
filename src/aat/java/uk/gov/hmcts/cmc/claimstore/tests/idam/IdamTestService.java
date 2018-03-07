package uk.gov.hmcts.cmc.claimstore.tests.idam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.AuthenticateUserResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.tests.AATConfiguration;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.TestData;

import java.util.Base64;

@Service
public class IdamTestService {
    private static final String PIN_PREFIX = "Pin ";

    private final IdamApi idamApi;
    private final IdamTestApi idamTestApi;
    private final UserService userService;
    private final TestData testData;
    private final AATConfiguration aatConfiguration;

    @Autowired
    public IdamTestService(
        IdamApi idamApi,
        IdamTestApi idamTestApi,
        UserService userService,
        TestData testData,
        AATConfiguration aatConfiguration
    ) {
        this.idamApi = idamApi;
        this.idamTestApi = idamTestApi;
        this.userService = userService;
        this.testData = testData;
        this.aatConfiguration = aatConfiguration;
    }

    public User createCitizen() {
        String email = testData.nextUserEmail();
        idamTestApi.createUser(createCitizenRequest(email, aatConfiguration.getSmokeTestCitizen().getPassword()));
        return userService.authenticateUser(email, aatConfiguration.getSmokeTestCitizen().getPassword());
    }

    public User createDefendant(final String hackHackHackClaimantId) {
        // HACK no way to know the letter holder ID currently so we pray claimantId + 1 works for now
        String letterHolderId = String.valueOf(Integer.valueOf(hackHackHackClaimantId) + 1);

        String email = testData.nextUserEmail();
        String password = aatConfiguration.getSmokeTestCitizen().getPassword();
        idamTestApi.createUser(createCitizenRequest(email, password));

        ResponseEntity<String> pin = idamTestApi.getPinByLetterHolderId(letterHolderId);
        String authorisation = PIN_PREFIX + new String(Base64.getEncoder().encode(pin.getBody().getBytes()));
        AuthenticateUserResponse pinUser = idamApi.authenticateUser(authorisation);

        idamApi.upliftUser(userService.getBasicAuthHeader(email, password), pinUser.getAccessToken());

        // Re-authenticate to get new roles on the user
        return userService.authenticateUser(email, password);
    }

    private CreateUserRequest createCitizenRequest(String username, String password) {
        return new CreateUserRequest(
            username,
            new UserGroup("cmc-private-beta"),
            password
        );
    }
}
