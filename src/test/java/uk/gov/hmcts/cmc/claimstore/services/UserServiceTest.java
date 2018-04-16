package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworkerProperties;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.Oauth2;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    private UserService userService;

    @Mock
    private IdamApi idamApi;

    @Mock
    private IdamCaseworkerProperties idamCaseworkerProperties;

    @Mock
    private Oauth2 oauth2;

    @Before
    public void setup() {
        userService = new UserService(idamApi, idamCaseworkerProperties, oauth2);
    }

    @Test
    public void shouldGetUserDetails() {
        //given
        String authorisationToken = "Open sesame!";
        UserDetails userDetails = SampleUserDetails.getDefault();
        when(idamApi.retrieveUserDetails(authorisationToken)).thenReturn(userDetails);

        //when
        UserDetails output = userService.getUserDetails(authorisationToken);

        //then
        assertThat(output).isNotNull().isEqualTo(userDetails);

        //verify
        verify(idamApi).retrieveUserDetails(authorisationToken);
    }
}
