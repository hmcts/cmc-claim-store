package uk.gov.hmcts.cmc.claimstore.tests.idam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.AuthenticateUserResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.tests.AATConfiguration;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.TestData;

import java.util.Base64;

@Service
public class IdamTestService {
    private static final String PIN = "Pin ";
    private static final String BEARER = "Bearer ";

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

    public User createDefendant(int letterHolderId) {
        ResponseEntity<String> pin = idamTestApi.getPinByLetterHolderId(letterHolderId);
        String authorisation =  PIN + new String(Base64.getEncoder().encode(pin.getBody().getBytes()));
        AuthenticateUserResponse authenticateUserResponse = idamApi.authenticateUser(authorisation);

        String email = testData.nextUserEmail();
        idamTestApi.createUser(createCitizenRequest(email, aatConfiguration.getSmokeTestCitizen().getPassword()));

        User defendant = userService.authenticateUser(email, aatConfiguration.getSmokeTestCitizen().getPassword());

        idamApi.register(defendant, BEARER + authenticateUserResponse.getAccessToken());
        return defendant;
    }

    private CreateUserRequest createCitizenRequest(String username, String password) {
        return new CreateUserRequest(
            username,
            new UserGroup("cmc-private-beta"),
            password
        );
    }
}
