package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.UserRolesRepository;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.UserRole;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.SUBMITTER_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class UserRolesServiceTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String USER_ID = "11";

    private static final UserDetails claimantDetails
        = SampleUserDetails.builder().withUserId(USER_ID).withMail(SUBMITTER_EMAIL).build();

    private static final String CONSENT_GIVEN_ROLE = "cmc-new-features-consent-given";

    @Mock
    private UserRolesRepository userRolesRepository;
    @Mock
    private UserService userService;

    private UserRolesService userRolesService;
    private final UserRole authorizedUserRole = new UserRole(USER_ID, CONSENT_GIVEN_ROLE);

    @Before
    public void setup() {
        when(userService.getUser(eq(AUTHORISATION))).thenReturn(new User(AUTHORISATION, claimantDetails));

        userRolesService = new UserRolesService(
            userRolesRepository,
            userService);
    }

    @Test
    public void retrieveUserRolesShouldReturnRoleFoundInRepository() {
        when(userRolesRepository.getByUserId(eq(USER_ID))).thenReturn(ImmutableList.of(authorizedUserRole));
        List<String> roles = userRolesService.retrieveUserRoles(AUTHORISATION);
        assertThat(roles).containsOnly(CONSENT_GIVEN_ROLE);
    }

    @Test
    public void retrieveUserRolesShouldReturnEmptyIfNotFound() {
        when(userRolesRepository.getByUserId(eq(USER_ID))).thenReturn(Collections.emptyList());
        List<String> roles = userRolesService.retrieveUserRoles(AUTHORISATION);
        assertThat(roles).isEmpty();
    }

    @Test
    public void saveUserRoleShouldFinishSuccessfully() {
        doNothing()
            .when(userRolesRepository).saveUserRole(eq(USER_ID), eq(authorizedUserRole.getRole()));

        userRolesService.saveRole(authorizedUserRole.getRole(), AUTHORISATION);

        verify(userRolesRepository, once())
            .saveUserRole(eq(USER_ID), eq(authorizedUserRole.getRole()));
    }
}
