package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.document.domain.Classification;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.DOCUMENT_NAME;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_UPLOAD_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false",
    }
)
@ActiveProfiles("test")
public class SaveClaimWithDocumentManagementTest extends BaseSaveTest {

    private static final String EXTENSION = ".pdf";

    @Test
    public void shouldUploadSealedCopyOfNonRepresentedClaimIntoDocumentManagementStore() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        MvcResult result = makeIssueClaimRequest(claimData, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        Claim claim = deserializeObjectFrom(result, Claim.class);

        postClaimOperation.getClaim(claim.getExternalId(), AUTHORISATION_TOKEN);

        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        InMemoryMultipartFile sealedClaimForm = new InMemoryMultipartFile(
            "files",
            buildSealedClaimFileBaseName(claim.getReferenceNumber()) + EXTENSION,
            MediaType.APPLICATION_PDF_VALUE,
            PDF_BYTES
        );
        InMemoryMultipartFile claimIssueReceipt = new InMemoryMultipartFile(
            "files",
            buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()) + EXTENSION,
            MediaType.APPLICATION_PDF_VALUE,
            PDF_BYTES
        );
        List<InMemoryMultipartFile> files = Arrays.asList(sealedClaimForm, claimIssueReceipt);
        verify(documentUploadClient, times(2)).upload(
            eq(AUTHORISATION_TOKEN),
            any(),
            any(),
            anyList(),
            any(Classification.class),
            argument.capture()
        );
        List<List> capturedArgument = argument.getAllValues();
        capturedArgument.forEach(fileList ->
            fileList.forEach(file -> assertTrue(files.contains(file)))
        );
    }

    @Test
    public void shouldUploadSealedCopyOfRepresentedClaimIntoDocumentManagementStore() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();
        given(documentUploadClient
            .upload(eq(SOLICITOR_AUTHORISATION_TOKEN), anyString(), anyString(),
                anyList(), any(Classification.class), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
        MvcResult result = makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        Claim claim = deserializeObjectFrom(result, Claim.class);
        InMemoryMultipartFile sealedClaimForm = new InMemoryMultipartFile(
            "files",
            buildSealedClaimFileBaseName(claim.getReferenceNumber()) + EXTENSION,
            MediaType.APPLICATION_PDF_VALUE,
            PDF_BYTES
        );
        verify(documentUploadClient, times(1)).upload(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            any(),
            any(),
            anyList(),
            any(Classification.class),
            argument.capture()
        );
        List<InMemoryMultipartFile> capturedArgument = argument.getValue();
        assertEquals(capturedArgument.size(), 1);
        assertTrue(capturedArgument.contains(sealedClaimForm));
    }

    @Test
    public void shouldLinkSealedCopyOfNonRepresentedClaimAfterUpload() throws Exception {
        assertDocumentIsLinked(SampleClaimData.submittedByClaimant(),
            AUTHORISATION_TOKEN,
            SEALED_CLAIM,
            "claim-form.pdf");
    }

    @Test
    public void shouldLinkClaimIssueReceiptClaimAfterUpload() throws Exception {
        assertDocumentIsLinked(SampleClaimData.submittedByClaimant(),
            AUTHORISATION_TOKEN,
            CLAIM_ISSUE_RECEIPT,
            "claim-form-claimant-copy.pdf");
    }

    @Test
    public void shouldLinkSealedCopyOfRepresentedClaimAfterUpload() throws Exception {
        assertDocumentIsLinked(SampleClaimData.submittedByLegalRepresentative(),
            SOLICITOR_AUTHORISATION_TOKEN,
            SEALED_CLAIM,
            "claim-form.pdf");
    }

    private void assertDocumentIsLinked(
        ClaimData claimData,
        String authorization,
        ClaimDocumentType claimDocumentType,
        String fileName
    ) throws Exception {

        given(documentUploadClient.upload(eq(authorization), any(), any(), anyList(), any(Classification.class), any()))
            .willReturn(successfulDocumentManagementUploadResponse());

        MvcResult result = makeIssueClaimRequest(claimData, authorization)
            .andExpect(status().isOk())
            .andReturn();

        Claim claim = deserializeObjectFrom(result, Claim.class);
        Claim resultWithDocument = postClaimOperation.getClaim(claim.getExternalId(), authorization);

        Optional<ClaimDocumentCollection> claimDocumentCollection = resultWithDocument.getClaimDocumentCollection();

        ClaimDocument claimDocument = claimDocumentCollection
            .orElseThrow(AssertionError::new)
            .getDocument(claimDocumentType)
            .orElseThrow(AssertionError::new);
        assertThat(claimDocument.getDocumentManagementUrl()
            .equals(URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4")));
        assertThat(claimDocument.getDocumentName()).endsWith(fileName);
    }

    @Test
    public void shouldNotReturn500HttpStatusAndShouldSendStaffEmailWhenDocumentUploadFailed() throws Exception {
        given(documentUploadClient
            .upload(eq(AUTHORISATION_TOKEN), anyString(), anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());

        MvcResult result = makeIssueClaimRequest(SampleClaimData
            .submittedByLegalRepresentativeBuilder().build(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        Claim claim = deserializeObjectFrom(result, Claim.class);

        Claim updated = postClaimOperation.getClaim(claim.getExternalId(), AUTHORISATION_TOKEN);

        assertThat(updated.getClaimSubmissionOperationIndicators())
            .isEqualTo(ClaimSubmissionOperationIndicators.builder().build());
    }

    @Test
    public void shouldRetryOnDocumentUploadFailures() throws Exception {
        given(documentUploadClient
            .upload(eq(AUTHORISATION_TOKEN), anyString(), anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse())
            .willReturn(unsuccessfulDocumentManagementUploadResponse())
            .willReturn(unsuccessfulDocumentManagementUploadResponse())
        ;

        when(authTokenGenerator.generate()).thenReturn(BEARER_TOKEN);

        makeIssueClaimRequest(SampleClaimData.submittedByLegalRepresentativeBuilder().build(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(documentUploadClient, times(3))
            .upload(eq(AUTHORISATION_TOKEN), anyString(), anyString(), anyList(), any(Classification.class), anyList());

        verify(appInsights).trackEvent(eq(DOCUMENT_MANAGEMENT_UPLOAD_FAILURE), eq(DOCUMENT_NAME), anyString());

    }
}
