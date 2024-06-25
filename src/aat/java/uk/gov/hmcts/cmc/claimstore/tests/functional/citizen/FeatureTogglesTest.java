package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.Retry;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.RetryFailedFunctionalTests;
import uk.gov.hmcts.cmc.domain.models.UserRoleRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureTogglesTest extends BaseTest {
    private static final String CONSENT_GIVEN_ROLE = "cmc-new-features-consent-given";

    private User user; //needs a new user with each test for guaranteed behaviour

    @BeforeEach
    public void before() {
        user = idamTestService.createCitizen();
    }

    @Rule
    public RetryFailedFunctionalTests retryRule = new RetryFailedFunctionalTests(3);

    @AfterEach
    public void after() {
        idamTestService.deleteUser(user.getUserDetails().getEmail());
    }

    @Test
    @Retry
    public void shouldSuccessfullySubmitUserRole() {
        commonOperations.saveUserRoles(new UserRoleRequest(CONSENT_GIVEN_ROLE), user.getAuthorisation())
            .then()
            .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    @Retry
    public void shouldThrow409OnSubmitOfSameUserRoleMoreThanOnce() {
        commonOperations.saveUserRoles(new UserRoleRequest(CONSENT_GIVEN_ROLE), user.getAuthorisation())
            .then()
            .statusCode(HttpStatus.CREATED.value());

        commonOperations.saveUserRoles(new UserRoleRequest(CONSENT_GIVEN_ROLE), user.getAuthorisation())
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Retry
    public void shouldSuccessfullyFetchRole() {
        commonOperations.saveUserRoles(new UserRoleRequest(CONSENT_GIVEN_ROLE), user.getAuthorisation())
            .then()
            .statusCode(HttpStatus.CREATED.value());

        List<String> roles = commonOperations.getUserRole(user.getAuthorisation())
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(List.class);

        assertThat(roles.get(0)).isEqualTo(CONSENT_GIVEN_ROLE);
    }
}
