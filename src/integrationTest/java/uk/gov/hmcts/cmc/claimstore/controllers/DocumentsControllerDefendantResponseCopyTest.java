package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.reform.cmc.pdf.service.client.exception.PDFServiceClientException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentsControllerDefendantResponseCopyTest extends BaseTest {

    private static final String EXTERNAL_ID = "1234dsfdf";

    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    @Mock
    private Claim claim;
    @Mock
    private DefendantResponse defendantResponse;

    @MockBean
    private DefendantResponseCopyService defendantResponseCopyService;

    @Test
    public void shouldReturnPdfDocumentIfEverythingIsFine() throws Exception {
        when(claimRepository.getClaimByExternalId(eq(EXTERNAL_ID))).thenReturn(Optional.of(claim));
        when(defendantResponseRepository.getByClaimId(anyLong())).thenReturn(Optional.of(defendantResponse));
        when(defendantResponseCopyService.createPdf(eq(claim), eq(defendantResponse))).thenReturn(PDF_BYTES);

        MvcResult result = webClient
            .perform(get("/documents/defendantResponseCopy/" + EXTERNAL_ID))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(PDF_BYTES);
    }

    @Test
    public void shouldReturnNotFoundWhenClaimIsNotFound() throws Exception {
        when(claimRepository.getClaimByExternalId(eq(EXTERNAL_ID))).thenReturn(Optional.empty());

        webClient
            .perform(get("/documents/defendantResponseCopy/" + EXTERNAL_ID))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturnNotFoundWhenResponseIsNotFound() throws Exception {
        when(claimRepository.getClaimByExternalId(eq(EXTERNAL_ID))).thenReturn(Optional.of(claim));
        when(defendantResponseRepository.getByClaimId(anyLong())).thenReturn(Optional.empty());

        webClient
            .perform(get("/documents/defendantResponseCopy/" + EXTERNAL_ID))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturnServerErrorWhenPdfGenerationFails() throws Exception {
        when(claimRepository.getClaimByExternalId(eq(EXTERNAL_ID))).thenReturn(Optional.of(claim));
        when(defendantResponseRepository.getByClaimId(anyLong())).thenReturn(Optional.of(defendantResponse));
        when(defendantResponseCopyService.createPdf(eq(claim), eq(defendantResponse)))
            .thenThrow(new PDFServiceClientException(new RuntimeException("Something bad happened!")));

        webClient
            .perform(get("/documents/defendantResponseCopy/" + EXTERNAL_ID))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

}
