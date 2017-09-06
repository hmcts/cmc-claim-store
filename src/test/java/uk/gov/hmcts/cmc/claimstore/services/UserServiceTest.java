package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.idam.client.IdamClient;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    private UserService userService;
    @Mock
    private IdamClient idamClient;

    @Before
    public void setup() {
        userService = new UserService(idamClient);
    }

    @Test
    public void shouldGetUserDetails() {
        //given
        final String authorisationToken = "Open sesame!";
        final UserDetails userDetails = SampleUserDetails.getDefault();
        when(idamClient.retrieveUserDetails(authorisationToken)).thenReturn(userDetails);

        //when
        UserDetails output = userService.getUserDetails(authorisationToken);

        //then
        assertThat(output).isNotNull().isEqualTo(userDetails);

        //verify
        verify(idamClient).retrieveUserDetails(authorisationToken);
    }

}
