package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.DocumentManagementBaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "feature_toggles.document_management=true"
    }
)
public class RetrieveRepresentedClaimCopyUsingDocumementManagementTest extends DocumentManagementBaseIntegrationTest {

    private static final String AUTH_TOKEN = "Bearer authDataString";
    public static final String DOCUMENT_SELF_PATH = "/documents/123-3434-232-3434";

    @Test
    public void shouldReturnPdfDocumentFromDocumentStoreAfterUploadIfClaimIsNotLinked() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder()
            .withAmount(SampleAmountRange.builder().build())
            .build());

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk())
            .andExpect(content().bytes(PDF_BYTES))
            .andReturn();

        verify(documentUploadClientApi).upload(anyString(), any(List.class));
        verify(documentMetadataDownloadApi).getDocumentMetadata(anyString(), any(String.class));
        verify(documentDownloadClientApi).downloadBinary(anyString(), any(String.class));
    }

    @Test
    public void shouldReturnPdfDocumentFromDocumentStoreIfEverythingIsFine() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder()
            .withAmount(SampleAmountRange.builder().build())
            .build());

        claim = claimStore.linkSealedClaimDocumentSelfPath(claim.getId(), DOCUMENT_SELF_PATH);

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk())
            .andExpect(content().bytes(PDF_BYTES))
            .andReturn();

        verify(documentMetadataDownloadApi).getDocumentMetadata(anyString(), any(String.class));
        verify(documentDownloadClientApi).downloadBinary(anyString(), any(String.class));
    }

    @Test
    public void shouldReturnServerErrorWhenDocumentUploadFails() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder()
            .withAmount(SampleAmountRange.builder().build())
            .build());

        given(documentUploadClientApi.upload(anyString(), any(List.class)))
            .willThrow(new DocumentManagementException("Something bad happened!"));

        makeRequest(claim.getExternalId())
            .andExpect(status().isInternalServerError());

        verify(documentUploadClientApi).upload(anyString(), any(List.class));
    }

    //TODO Document management service should throw DocumentManagementException when get fails
    @Test
    public void shouldReturnServerErrorWhenDocumentDownloadFails() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder()
            .withAmount(SampleAmountRange.builder().build())
            .build());

        claim = claimStore.linkSealedClaimDocumentSelfPath(claim.getId(), DOCUMENT_SELF_PATH);

        given(documentMetadataDownloadApi.getDocumentMetadata(anyString(), anyString()))
            .willThrow(new DocumentManagementException("Something bad happened!"));

        makeRequest(claim.getExternalId())
            .andExpect(status().isInternalServerError());

        verify(documentMetadataDownloadApi).getDocumentMetadata(anyString(), anyString());
    }

    private ResultActions makeRequest(String externalId) throws Exception {
        return webClient
            .perform(get("/documents/legalSealedClaim/" + externalId)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
            );
    }
}
