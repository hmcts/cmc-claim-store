package uk.gov.hmcts.cmc.claimstore.tests.idam;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
import feign.Response;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
import uk.gov.hmcts.cmc.claimstore.tests.exception.ForbiddenException;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.TestData;

import java.nio.charset.Charset;
import java.time.Duration;

import static uk.gov.hmcts.cmc.claimstore.services.UserService.AUTHORIZATION_CODE;

@Service
public class IdamTestService {

    private static final Logger logger = LoggerFactory.getLogger(IdamTestService.class);
    private static final RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
        .handle(FeignException.class)
        .withDelay(Duration.ofSeconds(5))
        .onRetry(r -> logger.warn("Retrying IdamTestService"))
        .withMaxRetries(5);

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
        return Failsafe.with(retryPolicy)
            .get(() -> {
                createUser(createSolicitorRequest(email, aatConfiguration.getSmokeTestSolicitor().getPassword()));
                return userService.authenticateUser(email, aatConfiguration.getSmokeTestSolicitor().getPassword());
            });
    }

    public User createCitizen() {
        String email = testData.nextUserEmail();
        return Failsafe.with(retryPolicy)
            .get(() -> {
                createUser(createCitizenRequest(email, aatConfiguration.getSmokeTestCitizen().getPassword()));
                return userService.authenticateUser(email, aatConfiguration.getSmokeTestCitizen().getPassword());
            });
    }

    public User upliftDefendant(final String letterHolderId, User defendant) {
        String email = defendant.getUserDetails().getEmail();
        String password = aatConfiguration.getSmokeTestCitizen().getPassword();
        return Failsafe.with(retryPolicy)
            .get(() -> {
                ResponseEntity<String> pin = idamTestApi.getPinByLetterHolderId(letterHolderId);

                AuthenticateUserResponse pinUserCode = authenticatePinUser(pin.getBody());

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
            });
    }

    public void deleteUser(String email) {
        Failsafe.with(retryPolicy).run(() -> idamTestApi.deleteUser(email));
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
            ImmutableList.of(new UserRole("citizen")),
            password
        );
    }

    private CreateUserRequest createSolicitorRequest(String username, String password) {
        return new CreateUserRequest(
            username,
            ImmutableList.of(new UserRole("solicitor"),
                new UserRole("caseworker-cmc-solicitor"),
                new UserRole("caseworker-cmc")),
            password
        );
    }

    private void createUser(CreateUserRequest userRequest) {
        try {
            idamTestApi.createUser(userRequest);
        } catch (ForbiddenException ex) {
            logger.warn("Ignoring 403 for IdamApi.createUser - user already exists");
        }
    }

}
