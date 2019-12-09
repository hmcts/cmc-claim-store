package uk.gov.hmcts.cmc.claimstore.deprecated.controllers.base;

import org.junit.Before;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public abstract class BaseDownloadDocumentTest extends BaseIntegrationTest {

    protected static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};
    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    protected static final String USER_ID = "1";
    protected static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withUserId(USER_ID)
        .withMail("submitter@example.com")
        .build();

    @Before
    public void setup() {
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(new User(AUTHORISATION_TOKEN, USER_DETAILS));
    }

    private final String documentType;

    public BaseDownloadDocumentTest(String documentType) {
        this.documentType = documentType;
    }

    protected ResultActions makeRequest(String externalId) throws Exception {
        return webClient
            .perform(get("/documents/" + documentType + "/" + externalId)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            );
    }
}
