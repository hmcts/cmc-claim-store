package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.UserRolesService;
import uk.gov.hmcts.cmc.domain.models.UserRole;
import uk.gov.hmcts.cmc.domain.models.UserRoleRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserRolesControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String CONSENT_GIVEN_ROLE = "cmc-new-features-consent-given";
    private static final String USER_ID = "11";
    private UserRolesController userRolesController;

    @Mock
    private UserRolesService userRolesService;
    private UserRole userRole = new UserRole(USER_ID, CONSENT_GIVEN_ROLE);

    @Before
    public void setup() {
        userRolesController = new UserRolesController(userRolesService);
    }

    @Test
    public void shouldSaveUserRoleInRepository() {
        //given
        doNothing().when(userRolesService).saveRole(eq(new UserRoleRequest(userRole.getRole())), eq(AUTHORISATION));

        //when
        userRolesController.save(new UserRoleRequest(userRole.getRole()), AUTHORISATION);

        //verify
        verify(userRolesService).saveRole(eq(new UserRoleRequest(userRole.getRole())), eq(AUTHORISATION));
    }

    @Test
    public void shouldReturnAuthorizedUserRoleFromRepositoryForClaimantId() {
        //given
        when(userRolesService.userRoles(eq(AUTHORISATION)))
            .thenReturn(ImmutableList.of(CONSENT_GIVEN_ROLE));

        //when
        List<String> output = userRolesController.getByUserId(AUTHORISATION);

        //then
        String role = output.stream().findAny().orElseThrow(() -> new NotFoundException("Role not found"));
        assertThat(role).isEqualTo(CONSENT_GIVEN_ROLE);
    }
}
