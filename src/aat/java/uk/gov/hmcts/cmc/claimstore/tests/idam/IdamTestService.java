package uk.gov.hmcts.cmc.claimstore.tests.idam;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;

import static java.lang.String.format;

@Service
public class IdamTestService {

    private final static String EMAIL_PATTERN = "aat-functional-testing-%s@server.com";

    private final IdamTestApi idamTestApi;
    private final UserService userService;
    private final String password;

    @Autowired
    public IdamTestService(
        IdamTestApi idamTestApi,
        UserService userService,
        @Value("${aat.test-user.password}") String password
    ) {
        this.idamTestApi = idamTestApi;
        this.userService = userService;
        this.password = password;
    }

    /**
     * @return created user's authentication token
     */
    public User createDefendant() {
        String email = format(EMAIL_PATTERN, RandomStringUtils.randomAlphanumeric(10));
        idamTestApi.createUser(createDefendantRequest(email, password));
        return userService.authenticateUser(email, password);
    }

    private CreateUserRequest createDefendantRequest(String username, String password) {
        return new CreateUserRequest(
            username,
            new UserGroup("cmc-private-beta"),
            password
        );
    }

}
