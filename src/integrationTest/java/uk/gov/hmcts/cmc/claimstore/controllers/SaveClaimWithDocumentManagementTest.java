package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@TestPropertySource(
    properties = {
        "document_management.url=http://localhost:8085",
        "feature_toggles.ccd_async_enabled=false",
        "feature_toggles.ccd_enabled=false"
    }
)
public class SaveClaimWithDocumentManagementTest extends BaseSaveTest {

    @Test
    public void shouldUploadSealedCopyOfNonRepresentedClaimIntoDocumentManagementStore() throws Exception {
        assertSealedClaimIsUploadedIntoDocumentManagementStore(SampleClaimData.submittedByClaimant(),
            AUTHORISATION_TOKEN);
    }

    @Test
    public void shouldUploadSealedCopyOfRepresentedClaimIntoDocumentManagementStore() throws Exception {
        assertSealedClaimIsUploadedIntoDocumentManagementStore(SampleClaimData.submittedByLegalRepresentative(),
            SOLICITOR_AUTHORISATION_TOKEN);
    }

    private void assertSealedClaimIsUploadedIntoDocumentManagementStore(
        ClaimData claimData,
        String authorization
    ) throws Exception {
        given(documentUploadClient.upload(eq(authorization), any(), any(), any()))
            .willReturn(successfulDocumentManagementUploadResponse());
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
        MvcResult result = makeIssueClaimRequest(claimData, authorization)
            .andExpect(status().isOk())
            .andReturn();
        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        InMemoryMultipartFile sealedClaimForm = new InMemoryMultipartFile(
            "files",
            deserializeObjectFrom(result, Claim.class).getReferenceNumber() + "-claim-form.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            PDF_BYTES
        );
        verify(documentUploadClient, atLeastOnce()).upload(
            eq(authorization),
            any(),
            any(),
            argument.capture()
        );
        verify(documentUploadClient, atMost(3)).upload(
            eq(authorization),
            any(),
            any(),
            argument.capture()
        );
        List<List> capturedArgument = argument.getAllValues();
        assertThat(capturedArgument.contains(Collections.singleton(sealedClaimForm)));
    }

    @Test
    public void shouldLinkSealedCopyOfNonRepresentedClaimAfterUpload() throws Exception {
        assertDocumentIsLinked(SampleClaimData.submittedByClaimant(),
            AUTHORISATION_TOKEN,
            SEALED_CLAIM,
            "000MC001-claim-form.pdf");
    }

    @Test
    public void shouldLinkDefendantPinLetterAfterUpload() throws Exception {
        assertDocumentIsLinked(SampleClaimData.submittedByClaimant(),
            AUTHORISATION_TOKEN,
            DEFENDANT_PIN_LETTER,
            "000MC001-defendant-pin-letter.pdf");
    }

    @Test
    public void shouldLinkClaimIssueReceiptClaimAfterUpload() throws Exception {
        assertDocumentIsLinked(SampleClaimData.submittedByClaimant(),
            AUTHORISATION_TOKEN,
            CLAIM_ISSUE_RECEIPT,
            "000MC001-claim-form-claimant-copy.pdf");
    }

    @Test
    public void shouldLinkSealedCopyOfRepresentedClaimAfterUpload() throws Exception {
        assertDocumentIsLinked(SampleClaimData.submittedByLegalRepresentative(),
            SOLICITOR_AUTHORISATION_TOKEN,
            SEALED_CLAIM,
            "000MC001-claim-form.pdf");
    }

    private void assertDocumentIsLinked(ClaimData claimData,
                                           String authorization,
                                           ClaimDocumentType claimDocumentType,
                                           String fileName) throws Exception {
        given(documentUploadClient.upload(eq(authorization), any(), any(), any()))
            .willReturn(successfulDocumentManagementUploadResponse());

        MvcResult result = makeIssueClaimRequest(claimData, authorization)
            .andExpect(status().isOk())
            .andReturn();

        MvcResult resultWithDocument = makeGetRequest("/claims/"
            + deserializeObjectFrom(result, Claim.class).getExternalId())
            .andExpect(status().isOk())
            .andReturn();

        Optional<ClaimDocumentCollection> claimDocumentCollection = deserializeObjectFrom(resultWithDocument,
            Claim.class).getClaimDocumentCollection();
        ClaimDocument claimDocument = claimDocumentCollection
            .orElseThrow(AssertionError::new)
            .getDocument(claimDocumentType)
            .orElseThrow(AssertionError::new);
        assertThat(claimDocument.getDocumentManagementUrl()
            .equals(URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4")));
        assertThat(claimDocument.getDocumentName().equals(fileName));
    }

    @Test
    public void shouldNotReturn500HttpStatusAndShouldSendStaffEmailWhenDocumentUploadFailed() throws Exception {
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), any(), any(), any()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());

        makeIssueClaimRequest(SampleClaimData.submittedByLegalRepresentativeBuilder().build(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(emailService, times(1)).sendEmail(anyString(), any());
    }

}
