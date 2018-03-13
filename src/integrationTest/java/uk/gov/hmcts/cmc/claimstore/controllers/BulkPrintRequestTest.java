package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=false",
        "send-letter.url=http://localhost:8089/send"
    }
)
@AutoConfigureWireMock(port = 8089)
public class BulkPrintRequestTest extends BaseSaveTest {

    @MockBean
    private BulkPrintStaffNotificationService bulkPrintNotificationService;

    @Test
    public void shouldNotSendNotificationWhenEverythingIsOk() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);

        stubFor(post(urlEqualTo("/send/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody("{ \"letter_id\":\"" + UUID.randomUUID().toString() + "\" }")
            )
        );

        MvcResult result = makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService, never())
            .notifyFailedBulkPrint(any(List.class), eq(deserializeObjectFrom(result, Claim.class)));
    }

    @Test
    public void shouldTrySendingLetterThreeTimesOnFailuresWhenBulkPrintFailsWithSendLetterException() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);

        stubFor(post(urlEqualTo("/send/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withHeader("Content-Type", "application/json")
            )
        );

        MvcResult result = makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService, atLeast(1))
            .notifyFailedBulkPrint(any(List.class), eq(deserializeObjectFrom(result, Claim.class)));
    }

    @Test
    public void shouldTrySendingLetterThreeTimesOnFailuresWhenBulkPrintFailsWithRestClientException() throws Exception {
        //given

        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);

        stubFor(post(urlEqualTo("/send/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withHeader("Content-Type", "application/json")
                .withBody("Internal server error occurred")));

        MvcResult result = makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService, atLeast(1))
            .notifyFailedBulkPrint(any(List.class), eq(deserializeObjectFrom(result, Claim.class)));
    }
}
