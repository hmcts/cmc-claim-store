package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.controllers.base.BaseDownloadDocumentTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.pdf.service.client.exception.PDFServiceClientException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class DownloadRepresentedClaimCopyTest extends BaseDownloadDocumentTest {

    public DownloadRepresentedClaimCopyTest() {
        super("legalSealedClaim");
    }

    @Test
    public void shouldReturnPdfDocumentIfEverythingIsFine() throws Exception {
        given(pdfServiceClient.generateFromHtml(any(), any()))
            .willReturn(PDF_BYTES);

        Claim claim = claimStore.saveClaim(SampleClaimData.submittedByLegalRepresentative());

        when(userService.getUserDetails(AUTHORISATION_TOKEN)).thenReturn(
            SampleUserDetails.builder().withUserId(claim.getSubmitterId()).build());

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
        given(pdfServiceClient.generateFromHtml(any(), any()))
            .willThrow(new PDFServiceClientException(new RuntimeException("Something bad happened!")));

        Claim claim = claimStore.saveClaim(SampleClaimData.submittedByLegalRepresentative());

        makeRequest(claim.getExternalId())
            .andExpect(status().isInternalServerError());
    }
}
