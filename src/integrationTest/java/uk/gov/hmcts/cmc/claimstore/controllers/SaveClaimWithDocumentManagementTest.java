package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class SaveClaimWithDocumentManagementTest extends BaseSaveTest {

    @Test
    public void shouldUploadSealedCopyOfNonRepresentedClaimIntoDocumentManagementStore() throws Exception {
        assertSealedClaimIsUploadedIntoDocumentManagementStore(SampleClaimData.submittedByClaimant());
    }

    @Test
    public void shouldUploadSealedCopyOfRepresentedClaimIntoDocumentManagementStore() throws Exception {
        assertSealedClaimIsUploadedIntoDocumentManagementStore(SampleClaimData.submittedByLegalRepresentative());
    }

    private void assertSealedClaimIsUploadedIntoDocumentManagementStore(ClaimData claimData) throws Exception {
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        verify(documentUploadClient).upload(AUTHORISATION_TOKEN, newArrayList(new InMemoryMultipartFile("files",
            deserializeObjectFrom(result, Claim.class).getReferenceNumber() + "-claim-form.pdf",
            "application/pdf", PDF_BYTES)
        ));
    }

    @Test
    public void shouldLinkSealedCopyOfNonRepresentedClaimAfterUpload() throws Exception {
        assertSealedClaimIsLinked(SampleClaimData.submittedByClaimant());
    }

    @Test
    public void shouldLinkSealedCopyOfRepresentedClaimAfterUpload() throws Exception {
        assertSealedClaimIsLinked(SampleClaimData.submittedByLegalRepresentative());
    }

    private void assertSealedClaimIsLinked(ClaimData claimData) throws Exception {
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class).getSealedClaimDocumentSelfPath())
            .isEqualTo(Optional.of("/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4"));
    }

    @Test
    public void shouldReturn500HttpStatusAndNotSendStaffEmailWhenDocumentUploadFailed() throws Exception {
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), anyList()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());

        makeRequest(SampleClaimData.submittedByLegalRepresentativeBuilder().build())
            .andExpect(status().isInternalServerError());

        verify(emailService, never()).sendEmail(anyString(), any());
    }

}
