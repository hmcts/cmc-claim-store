package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.UserRoleRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureTogglesTest extends BaseTest {
    private static final String CONSENT_GIVEN_ROLE = "cmc-new-features-consent-given";

    private User user; //needs a new user with each test for guaranteed behaviour

    @Before
    public void before() {
        user = idamTestService.createCitizen();
    }

    @After
    public void after() {
        idamTestService.deleteUser(user.getUserDetails().getEmail());
    }

    @Test
    public void shouldSuccessfullySubmitUserRole() {
        commonOperations.saveUserRoles(new UserRoleRequest(CONSENT_GIVEN_ROLE), user.getAuthorisation())
            .then()
            .statusCode(HttpStatus.CREATED.value());
    }

    @Test
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
