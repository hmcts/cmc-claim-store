package uk.gov.hmcts.cmc.claimstore.controllers.base;

import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public abstract class BaseDownloadDocumentTest extends BaseIntegrationTest {

    protected static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};
    protected static final String AUTHORISATION_TOKEN = "Bearer token";

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
