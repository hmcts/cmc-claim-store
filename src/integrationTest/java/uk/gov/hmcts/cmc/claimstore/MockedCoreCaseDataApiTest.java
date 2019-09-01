package uk.gov.hmcts.cmc.claimstore;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_CASE;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitRepresentativeResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api"
    }
)
@AutoConfigureWireMock(port = 2345)
public class MockedCoreCaseDataApi extends BaseSaveTest {

    @Autowired
    private WireMockServer wireMockServer;

    public void shouldPerformSimpleTesting(){
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);
        stubForStartForCaseworker();
        stubForSubmitForCaseworker();
        stubForStartEventForCaseWorker();
        stubForSubmitEventForCaseWorker();

        MvcResult result = makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
    }

    public void stubForStartForCaseworker() {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID
                           +"/event-triggers/" + CREATE_CASE.getValue() + "/token";

        wireMockServer.stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SERVICE_TOKEN))
            .willReturn(okForJson(successfulCoreCaseDataStoreStartResponse()))
        );
    }

    public void stubForSubmitForCaseworker() {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID
                           +"/case-types/"+ CASE_TYPE_ID +"/cases";

        wireMockServer.stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SERVICE_TOKEN))
            .withQueryParam("ignore-warning", equalTo(String.valueOf(IGNORE_WARNING)))
            .withRequestBody(equalTo(""))
            .willReturn(okForJson(successfulCoreCaseDataStoreSubmitRepresentativeResponse()))
        );
    }

    public void stubForStartEventForCaseWorker() {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID
                           +"/cases/"+ " " +"/event-triggers/"+ ISSUE_CASE.getValue() + "/token";

        wireMockServer.stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SERVICE_TOKEN))
            .willReturn(okForJson(successfulCoreCaseDataStoreStartResponse()))
        );
    }

    public void stubForSubmitEventForCaseWorker() {
        final String URI = "/caseworkers/"+ USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"
                           + CASE_TYPE_ID +"/cases/"+ " " +"/events";

        wireMockServer.stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SERVICE_TOKEN))
            .withQueryParam("ignore-warning", equalTo(String.valueOf(IGNORE_WARNING)))
            .withRequestBody(equalTo(""))
            .willReturn(okForJson(successfulCoreCaseDataStoreSubmitRepresentativeResponse()))
        );
    }
}
