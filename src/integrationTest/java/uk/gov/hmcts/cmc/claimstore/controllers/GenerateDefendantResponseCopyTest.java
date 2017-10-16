package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.exception.PDFServiceClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GenerateDefendantResponseCopyTest extends BaseTest {

    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    @MockBean
    private DefendantResponseCopyService defendantResponseCopyService;

    @Test
    public void shouldReturnPdfDocumentIfEverythingIsFine() throws Exception {
        Claim claim = claimStore.save(SampleClaimData.builder().build());

        when(defendantResponseCopyService.createPdf(claim)).thenReturn(PDF_BYTES);

        MvcResult result = webClient
            .perform(get("/documents/defendantResponseCopy/" + claim.getExternalId()))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(PDF_BYTES);
    }

    @Test
    public void shouldReturnNotFoundWhenClaimIsNotFound() throws Exception {
        String nonExistingExternalId = "f5b92e36-fc9c-49e6-99f7-74d60aaa8da2";

        webClient
            .perform(get("/documents/defendantResponseCopy/" + nonExistingExternalId))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnServerErrorWhenPdfGenerationFails() throws Exception {
        Claim claim = claimStore.save(SampleClaimData.builder().build());

        when(defendantResponseCopyService.createPdf(claim))
            .thenThrow(new PDFServiceClientException(new RuntimeException("Something bad happened!")));

        webClient
            .perform(get("/documents/defendantResponseCopy/" + claim.getExternalId()))
            .andExpect(status().isInternalServerError());
    }

}
