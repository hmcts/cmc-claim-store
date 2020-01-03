package uk.gov.hmcts.cmc.claimstore.deprecated;

import org.junit.Before;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public abstract class BaseGetTest extends BaseIntegrationTest {

    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    protected static final String USER_ID = "1";
    protected static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withUserId(USER_ID)
        .withMail("submitter@example.com")
        .build();

    protected static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    @Before
    public void setup() {
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(USER_DETAILS);
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(new User(AUTHORISATION_TOKEN, USER_DETAILS));
    }

    protected ResultActions makeRequest(String urlTemplate) throws Exception {
        return webClient.perform(
            get(urlTemplate)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
        );
    }
}
