package uk.gov.hmcts.cmc.claimstore;

import org.junit.Before;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;

public abstract class BaseSaveTest extends BaseIntegrationTest {
    public static final String ANONYMOUS_BEARER_TOKEN = "Anonymous Bearer token";
    public static final String ANONYMOUS_USER_ID = "3";

    @Before
    public void setup() {
        UserDetails userDetails = SampleUserDetails.builder().build();
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(new User(AUTHORISATION_TOKEN, userDetails));

        UserDetails solicitorDetails = SampleUserDetails.builder().withRoles("solicitor").build();
        given(userService.getUserDetails(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(solicitorDetails);
        given(userService.getUser(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(new User(SOLICITOR_AUTHORISATION_TOKEN, solicitorDetails));

        given(userService.generatePin("John Smith", AUTHORISATION_TOKEN))
            .willReturn(new GeneratePinResponse("my-pin", "2"));

        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(PDF_BYTES);

        given(userService.authenticateAnonymousCaseWorker())
            .willReturn(new User(ANONYMOUS_BEARER_TOKEN,
                SampleUserDetails.builder().withUserId(ANONYMOUS_USER_ID).build()));
    }
}
