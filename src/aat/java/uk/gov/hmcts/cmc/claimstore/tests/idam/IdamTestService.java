package uk.gov.hmcts.cmc.claimstore.tests.idam;

import feign.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.TacticalIdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.AuthenticateUserResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.Oauth2;
import uk.gov.hmcts.cmc.claimstore.idam.models.TokenExchangeResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.tests.AATConfiguration;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.TestData;

import java.util.Base64;

import static uk.gov.hmcts.cmc.claimstore.services.UserService.AUTHORIZATION_CODE;

@Service
public class IdamTestService {
    private static final String PIN_PREFIX = "Pin ";

    private final TacticalIdamApi tacticalIdamApi;
    private final IdamApi idamApi;
    private final IdamTestApi idamTestApi;
    private final IdamInternalApi idamInternalApi;
    private final UserService userService;
    private final TestData testData;
    private final AATConfiguration aatConfiguration;
    private final Oauth2 oauth2;
    private final boolean strategicIdam;

    @Autowired
    public IdamTestService(
        TacticalIdamApi tacticalIdamApi,
        IdamApi idamApi,
        IdamTestApi idamTestApi,
        IdamInternalApi idamInternalApi,
        UserService userService,
        TestData testData,
        AATConfiguration aatConfiguration,
        Oauth2 oauth2,
        @Value("${idam.api.url}") String idamUrl
    ) {
        this.tacticalIdamApi = tacticalIdamApi;
        this.idamApi = idamApi;
        this.idamTestApi = idamTestApi;
        this.idamInternalApi = idamInternalApi;
        this.userService = userService;
        this.testData = testData;
        this.aatConfiguration = aatConfiguration;
        this.oauth2 = oauth2;
        this.strategicIdam = StringUtils.contains(idamUrl, "core-compute")
            || StringUtils.contains(idamUrl, "platform.hmcts.net");
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
        if (strategicIdam) {
            Response response = idamInternalApi.upliftUser(
                email,
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

        } else {
            tacticalIdamApi.upliftUser(
                userService.getBasicAuthHeader(email, password),
                exchangeResponse.getAccessToken(),
                UserService.CODE,
                oauth2.getClientId(),
                oauth2.getRedirectUrl()
            );
        }
    }

    private AuthenticateUserResponse authenticatePinUser(String pin) {
        AuthenticateUserResponse pinUserCode;
        if (strategicIdam) {
            Response response = idamInternalApi.authenticatePinUser(
                pin,
                oauth2.getClientId(),
                oauth2.getRedirectUrl()
            );

            String code = getCodeFromRedirect(response);
            pinUserCode = new AuthenticateUserResponse(code);

        } else {
            String authorisation = PIN_PREFIX + new String(Base64.getEncoder().encode(pin.getBytes()));

            pinUserCode = tacticalIdamApi.authenticatePinUser(
                authorisation,
                UserService.CODE,
                oauth2.getClientId(),
                oauth2.getRedirectUrl()
            );
        }
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
            new UserGroup("cmc-private-beta"),
            password
        );
    }
}
