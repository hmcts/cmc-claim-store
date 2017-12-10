package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    public static final String BEARER_TOKEN = "bearer Token";
    private UserService userService;

    @Mock
    private IdamApi idamApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Before
    public void setup() {
        userService = new UserService(idamApi, authTokenGenerator);
    }

    @Test
    public void shouldGetUserDetails() {
        //given
        final String authorisationToken = "Open sesame!";
        final UserDetails userDetails = SampleUserDetails.getDefault();
        when(idamApi.retrieveUserDetails(authorisationToken)).thenReturn(userDetails);

        //when
        UserDetails output = userService.getUserDetails(authorisationToken);

        //then
        assertThat(output).isNotNull().isEqualTo(userDetails);

        //verify
        verify(idamApi).retrieveUserDetails(authorisationToken);
    }

    @Test
    public void shouldGenerateServiceToken() {
        //given
        when(authTokenGenerator.generate()).thenReturn(BEARER_TOKEN);

        //when
        String output = userService.generateServiceAuthToken();

        //then
        assertThat(output).isNotNull().isEqualTo(BEARER_TOKEN);

        //verify
        verify(authTokenGenerator).generate();
    }
}
