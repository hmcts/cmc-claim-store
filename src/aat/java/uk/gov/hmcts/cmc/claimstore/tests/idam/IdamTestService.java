package uk.gov.hmcts.cmc.claimstore.tests.idam;

import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.AuthenticateUserResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.Oauth2;
import uk.gov.hmcts.cmc.claimstore.idam.models.TokenExchangeResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.tests.AATConfiguration;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.TestData;

import java.nio.charset.Charset;

import static uk.gov.hmcts.cmc.claimstore.services.UserService.AUTHORIZATION_CODE;

@Service
public class IdamTestService {

    private static final String PIN_PREFIX = "Pin ";

    private final IdamApi idamApi;
    private final IdamTestApi idamTestApi;
    private final IdamInternalApi idamInternalApi;
    private final UserService userService;
    private final TestData testData;
    private final AATConfiguration aatConfiguration;
    private final Oauth2 oauth2;

    @Autowired
    public IdamTestService(
        IdamApi idamApi,
        IdamTestApi idamTestApi,
        IdamInternalApi idamInternalApi,
        UserService userService,
        TestData testData,
        AATConfiguration aatConfiguration,
        Oauth2 oauth2,
        @Value("${idam.api.url}") String idamUrl
    ) {
        this.idamApi = idamApi;
        this.idamTestApi = idamTestApi;
        this.idamInternalApi = idamInternalApi;
        this.userService = userService;
        this.testData = testData;
        this.aatConfiguration = aatConfiguration;
        this.oauth2 = oauth2;
    }

    public User createSolicitor() {
        String email = testData.nextUserEmail();
        idamTestApi.createUser(createSolicitorRequest(email, aatConfiguration.getSmokeTestSolicitor().getPassword()));
        return userService.authenticateUser(email, aatConfiguration.getSmokeTestSolicitor().getPassword());
    }

    public User createCitizen() {
        String email = testData.nextUserEmail();
        idamTestApi.createUser(createCitizenRequest(email, aatConfiguration.getSmokeTestCitizen().getPassword()));
        return userService.authenticateUser(email, aatConfiguration.getSmokeTestCitizen().getPassword());
    }

    public User createDefendant(final String letterHolderId) {
        String email = testData.nextUserEmail();
        String password = aatConfiguration.getSmokeTestCitizen().getPassword();
        idamTestApi.createUser(createCitizenRequest(email, password));

        String pin = idamTestApi.getPinByLetterHolderId(letterHolderId);

        AuthenticateUserResponse pinUserCode = authenticatePinUser(pin);

        TokenExchangeResponse exchangeResponse = idamApi.exchangeCode(
            pinUserCode.getCode(),
            AUTHORIZATION_CODE,
            oauth2.getRedirectUrl(),
            oauth2.getClientId(),
            oauth2.getClientSecret()
        );

        upliftUser(email, password, exchangeResponse);

        // Re-authenticate to get new roles on the user
        return userService.authenticateUser(email, password);
    }

    private void upliftUser(String email, String password, TokenExchangeResponse exchangeResponse) {
        Response response = idamInternalApi.upliftUser(
            UriUtils.encode(email, Charset.forName("UTF-8")),
            password,
            exchangeResponse.getAccessToken(),
            oauth2.getClientId(),
            oauth2.getRedirectUrl()
        );

        String code = getCodeFromRedirect(response);

        idamApi.exchangeCode(
            code,
            AUTHORIZATION_CODE,
            oauth2.getRedirectUrl(),
            oauth2.getClientId(),
            oauth2.getClientSecret()
        );
    }

    private AuthenticateUserResponse authenticatePinUser(String pin) {
        AuthenticateUserResponse pinUserCode;

        Response response = idamInternalApi.authenticatePinUser(
            pin,
            oauth2.getClientId(),
            oauth2.getRedirectUrl()
        );

        String code = getCodeFromRedirect(response);
        pinUserCode = new AuthenticateUserResponse(code);

        return pinUserCode;
    }

    private String getCodeFromRedirect(Response response) {
        String location = response.headers().get("Location").stream().findFirst()
            .orElseThrow(IllegalArgumentException::new);

        UriComponents build = UriComponentsBuilder.fromUriString(location).build();
        return build.getQueryParams().getFirst("code");
    }

    private CreateUserRequest createCitizenRequest(String username, String password) {
        return new CreateUserRequest(
            username,
            new UserGroup("citizens"),
            password
        );
    }

    private CreateUserRequest createSolicitorRequest(String username, String password) {
        return new CreateUserRequest(
            username,
            new UserGroup("cmc-solicitor"),
            password
        );
    }
}
