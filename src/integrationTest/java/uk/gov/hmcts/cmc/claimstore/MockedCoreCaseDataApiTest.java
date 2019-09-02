package uk.gov.hmcts.cmc.claimstore;

import org.junit.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_CASE;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitRepresentativeResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://localhost:${wiremock.server.port}"
    }
)
@AutoConfigureWireMock(port = 0)
public class MockedCoreCaseDataApiTest extends BaseSaveTest {

    @Test
    public void shouldPerformSimpleTesting() throws Exception {

        final ClaimData submittedByLegalRepresentative = SampleClaimData.submittedByLegalRepresentative();
        final String externalID = submittedByLegalRepresentative.getExternalId().toString();

        stubForSearchForCaseWorkers(externalID);
        stubForStartForCaseworker();
        stubForSubmitForCaseworker(externalID);

        given(authTokenGenerator.generate()).willReturn(SOLICITOR_AUTHORISATION_TOKEN);

        makeIssueClaimRequest(submittedByLegalRepresentative, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
    }

    public void stubForSearchForCaseWorkers(String externalID) {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID + "/cases"
            + "?" + "case.externalId=" + externalID + "&" + "sortDirection=desc" + "&" + "page=1";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(okForJson(Collections.emptyList()))
        );
    }

    public void stubForStartForCaseworker() {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID
                           +"/event-triggers/" + CREATE_CASE.getValue() + "/token";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(okForJson(successfulCoreCaseDataStoreStartResponse()))
        );
    }

    public void stubForSubmitForCaseworker(String externalID) {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID
                           +"/case-types/"+ CASE_TYPE_ID +"/cases" + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .withRequestBody(containing(externalID))
            .willReturn(okForJson(successfulCoreCaseDataStoreSubmitRepresentativeResponse()))
        );
    }

    public void stubForStartEventForCaseWorker() {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID
                           +"/cases/"+ " " +"/event-triggers/"+ ISSUE_CASE.getValue() + "/token";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(okForJson(successfulCoreCaseDataStoreStartResponse()))
        );
    }


   public void stubForSubmitEventForCaseWorker() {
        final String URI = "/caseworkers/"+ USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"
                           + CASE_TYPE_ID +"/cases/"+ " " +"/events" + "?" + "ignore-warning" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .withRequestBody(equalTo(""))
            .willReturn(okForJson(successfulCoreCaseDataStoreSubmitRepresentativeResponse()))
        );
    }

}
