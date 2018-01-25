package uk.gov.hmcts.cmc.claimstore;

import org.junit.Before;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

public abstract class BaseSaveTest extends BaseIntegrationTest {

    protected static final String AUTHORISATION_TOKEN = "Bearer token";

    protected static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    @Before
    public void setup() {
        given(userService.getUserDetails(AUTHORISATION_TOKEN))
            .willReturn(SampleUserDetails.builder().build());

        given(userService.generatePin("John Smith", AUTHORISATION_TOKEN))
            .willReturn(new GeneratePinResponse("my-pin", "2"));

        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(PDF_BYTES);

        given(jwtHelper.isSolicitor(anyString())).willReturn(false);
    }

    protected ResultActions makeRequest(ClaimData claimData) throws Exception {
        return webClient
            .perform(post("/claims/" + USER_ID)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMapper.toJson(claimData))
            );
    }
}
