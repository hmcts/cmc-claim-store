package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseSaveTest;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false",
        "send-letter.url=http://localhost:${wiremock.server.port}"
    }
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
public class BulkPrintRequestTest extends BaseSaveTest {

    @Autowired
    private WireMockServer wireMockServer;

    @MockBean
    private BulkPrintStaffNotificationService bulkPrintNotificationService;

    @Test
    public void shouldNotSendNotificationWhenEverythingIsOk() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);

        wireMockServer.stubFor(post(urlEqualTo("/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{ \"letter_id\":\"" + UUID.randomUUID().toString() + "\" }")
            )
        );

        MvcResult result = makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = deserializeObjectFrom(result, Claim.class);

        postClaimOperation.getClaim(savedClaim.getExternalId(), AUTHORISATION_TOKEN);

        verify(bulkPrintNotificationService, never())
            .notifyFailedBulkPrint(
                anyList(),
                eq(deserializeObjectFrom(result, Claim.class)));
    }

    @Test
    public void shouldSendNotificationWhenBulkPrintFailsWithHttpClientError() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);

        wireMockServer.stubFor(post(urlEqualTo("/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            )
        );

        makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService)
            .notifyFailedBulkPrint(
                anyList(),
                any(Claim.class));
    }

    @Test
    public void shouldSendNotificationWhenBulkPrintFailsWithHttpServerError() throws Exception {

        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);

        wireMockServer.stubFor(post(urlEqualTo("/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("Internal server error occurred")));

        makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService)
            .notifyFailedBulkPrint(
                anyList(),
                any(Claim.class));
    }
}
