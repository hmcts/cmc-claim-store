package uk.gov.hmcts.cmc.claimstore.tests.idam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.ActivationData;
import uk.gov.hmcts.cmc.claimstore.idam.models.AuthenticateUserResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.tests.AATConfiguration;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.TestData;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class IdamTestService {
    private static final String PIN = "Pin ";
    private static final String BEARER = "Bearer ";

    private final IdamApi idamApi;
    private final IdamTestApi idamTestApi;
    private final IdamTestEmailApi idamTestEmailApi;
    private final UserService userService;
    private final TestData testData;
    private final AATConfiguration aatConfiguration;

    @Autowired
    public IdamTestService(
        IdamApi idamApi,
        IdamTestApi idamTestApi,
        IdamTestEmailApi idamTestEmailApi,
        UserService userService,
        TestData testData,
        AATConfiguration aatConfiguration
    ) {
        this.idamApi = idamApi;
        this.idamTestApi = idamTestApi;
        this.idamTestEmailApi = idamTestEmailApi;
        this.userService = userService;
        this.testData = testData;
        this.aatConfiguration = aatConfiguration;
    }

    public User createCitizen() {
        String email = testData.nextUserEmail();
        idamTestApi.createUser(createCitizenRequest(email, aatConfiguration.getSmokeTestCitizen().getPassword()));
        return userService.authenticateUser(email, aatConfiguration.getSmokeTestCitizen().getPassword());
    }

    public User createDefendant(String letterHolderId) {
        ResponseEntity<String> pin = idamTestApi.getPinByLetterHolderId(letterHolderId);
        String authorisation = PIN + new String(Base64.getEncoder().encode(pin.getBody().getBytes()));
        AuthenticateUserResponse authenticateUserResponse = idamApi.authenticateUser(authorisation);

        String email = testData.nextUserEmail();
        idamApi.register(new UserDetails(null, email, "john", "smith", null),
            BEARER + authenticateUserResponse.getAccessToken());

        ResponseEntity<?> activationEmail = idamTestEmailApi.getActivationEmail();
        String body = (String) activationEmail.getBody();

        ActivationData data = new ActivationData(aatConfiguration.getSmokeTestCitizen().getPassword());
        ResponseEntity<?> activate = idamApi.activate(data, BEARER + body.split("=")[1]);

        Map result = (LinkedHashMap) activate.getBody();
        String defendantAuth = BEARER + result.get("access_token");
        return new User(defendantAuth, idamApi.retrieveUserDetails(defendantAuth));
    }

    private CreateUserRequest createCitizenRequest(String username, String password) {
        return new CreateUserRequest(
            username,
            new UserGroup("cmc-private-beta"),
            password
        );
    }
}
