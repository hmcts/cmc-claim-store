package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

        MvcResult result = makeIssueClaimRequest(claimData, authorization)
            .andExpect(status().isOk())
            .andReturn();

        verify(documentUploadClient).upload(
            eq(authorization),
            any(),
            any(),
            eq(
                newArrayList(
                    new InMemoryMultipartFile(
                        "files",
                        deserializeObjectFrom(result, Claim.class).getReferenceNumber() + "-claim-form.pdf",
                        MediaType.APPLICATION_PDF_VALUE,
                        PDF_BYTES
                    )
                )
            )
        );
    }

    @Test
    public void shouldLinkSealedCopyOfNonRepresentedClaimAfterUpload() throws Exception {
        assertSealedClaimIsLinked(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN);
    }

    @Test
    public void shouldLinkSealedCopyOfRepresentedClaimAfterUpload() throws Exception {
        assertSealedClaimIsLinked(SampleClaimData.submittedByLegalRepresentative(), SOLICITOR_AUTHORISATION_TOKEN);
    }

    private void assertSealedClaimIsLinked(ClaimData claimData, String authorization) throws Exception {
        given(documentUploadClient.upload(eq(authorization), any(), any(), any()))
            .willReturn(successfulDocumentManagementUploadResponse());

        MvcResult result = makeIssueClaimRequest(claimData, authorization)
            .andExpect(status().isOk())
            .andReturn();

        MvcResult resultWithDocument = makeGetRequest("/claims/"
            + deserializeObjectFrom(result, Claim.class).getExternalId())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(resultWithDocument, Claim.class)
            .getSealedClaimDocument())
            .isEqualTo(Optional.of(URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4")));
    }

    @Test
    public void shouldReturn500HttpStatusAndNotSendStaffEmailWhenDocumentUploadFailed() throws Exception {
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), any(), any(), any()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());

        makeIssueClaimRequest(SampleClaimData.submittedByLegalRepresentativeBuilder().build(), AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError());

        verify(emailService, never()).sendEmail(anyString(), any());
    }

}
