package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.controllers.base.BaseDownloadDocumentTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class DownloadRepresentedClaimCopyWithDocumentManagementTest extends BaseDownloadDocumentTest {

    public DownloadRepresentedClaimCopyWithDocumentManagementTest() {
        super("legalSealedClaim");
    }

    @Before
    public void setup() {
        given(pdfServiceClient.generateFromHtml(any(), any()))
            .willReturn(PDF_BYTES);

        given(userService.getUserDetails(any())).willReturn(SampleUserDetails.getDefault());
    }

    @Test
    public void shouldUploadSealedClaimWhenDocumentHasNotBeenUploadedYet() throws Exception {
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), any(), any(), any()))
            .willReturn(successfulDocumentManagementUploadResponse());

        Claim claim = claimStore.saveClaim(SampleClaimData.submittedByLegalRepresentative());

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk())
            .andExpect(content().bytes(PDF_BYTES));

        verify(documentUploadClient).upload(eq(AUTHORISATION_TOKEN), any(), any(),
            eq(newArrayList(new InMemoryMultipartFile("files",
            claim.getReferenceNumber() + "-claim-form.pdf", "application/pdf", PDF_BYTES)))
        );
    }

    @Test
    public void shouldLinkSealedClaimWhenDocumentHasNotBeenUploadedYet() throws Exception {
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), any(), any(), any()))
            .willReturn(successfulDocumentManagementUploadResponse());

        Claim claim = claimStore.saveClaim(SampleClaimData.submittedByLegalRepresentative());

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk())
            .andExpect(content().bytes(PDF_BYTES));

        assertThat(claimStore.getClaim(claim.getId()).getSealedClaimDocument())
            .isEqualTo(Optional.of(URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4")));
    }

    @Test
    public void shouldNotReturnServerErrorWhenUploadToDocumentManagementStoreFailed() throws Exception {
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), any(), any(), any()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());

        Claim claim = claimStore.saveClaim(SampleClaimData.submittedByLegalRepresentative());

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk())
            .andExpect(content().bytes(PDF_BYTES));

        assertThat(claimStore.getClaim(claim.getId()).getSealedClaimDocument())
            .isEqualTo(Optional.empty());

    }
}
