package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.pdf.service.client.exception.PDFServiceClientException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GenerateDefendantResponseReceiptTest extends BaseIntegrationTest {

    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    @Test
    public void shouldReturnPdfDocumentIfEverythingIsFine() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        claimStore.saveResponse(claim.getId(), SampleResponse.FullDefence.builder().build());

        given(pdfServiceClient.generateFromHtml(any(), any()))
            .willReturn(PDF_BYTES);

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk())
            .andExpect(content().bytes(PDF_BYTES))
            .andReturn();
    }

    @Test
    public void shouldReturnNotFoundWhenClaimIsNotFound() throws Exception {
        String nonExistingExternalId = "f5b92e36-fc9c-49e6-99f7-74d60aaa8da2";

        makeRequest(nonExistingExternalId)
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnServerErrorWhenPdfGenerationFails() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        claimStore.saveResponse(claim.getId(), SampleResponse.FullDefence.builder().build());

        given(pdfServiceClient.generateFromHtml(any(), any()))
            .willThrow(new PDFServiceClientException(new RuntimeException("Something bad happened!")));

        makeRequest(claim.getExternalId())
            .andExpect(status().isInternalServerError());
    }

    private ResultActions makeRequest(String externalId) throws Exception {
        return webClient
            .perform(get("/documents/defendantResponseReceipt/" + externalId));
    }
}
