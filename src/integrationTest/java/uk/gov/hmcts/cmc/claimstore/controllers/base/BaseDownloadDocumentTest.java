package uk.gov.hmcts.cmc.claimstore.controllers.base;

import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.MockedCoreCaseDataApiTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class BaseDownloadDocumentTest extends MockedCoreCaseDataApiTest {

    private final String documentType;

    public BaseDownloadDocumentTest(String documentType) {
        this.documentType = documentType;
    }

    protected ResultActions makeRequest(String externalId) throws Exception {
        return webClient
            .perform(get("/documents/" + documentType + "/" + externalId)
                .header(HttpHeaders.AUTHORIZATION, SOLICITOR_AUTHORISATION_TOKEN)
            );
    }
}
