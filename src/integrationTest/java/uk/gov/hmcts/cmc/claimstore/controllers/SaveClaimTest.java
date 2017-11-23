package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.DocumentManagementBaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.failedDocumentManagementUpload;

@TestPropertySource(
    properties = {
        "feature_toggles.document_management=true"
    }
)
public class SaveClaimTest extends DocumentManagementBaseIntegrationTest {

    @Before
    public void setup() {
        given(userService.getUserDetails("token"))
            .willReturn(SampleUserDetails.builder().withUserId("1").withMail("claimant@email.com").build());

        given(userService.generatePin("John Smith", "token"))
            .willReturn(new GeneratePinResponse("my-pin", "2"));
    }

    @Test
    public void shouldUploadSealedClaimFormToDocumentStoreWhenLegalClaimIssuedEvent() throws Exception {
        ClaimData claimData = SampleClaimData.builder()
            .withAmount(SampleAmountRange.validDefaults())
            .build();

        makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        verify(documentUploadClientApi).upload(anyString(), any(List.class));
    }


    @Test
    public void shouldFailWhenDocumentStoreFailsUploadWhenLegalClaimIssuedEvent() throws Exception {
        given(documentUploadClientApi.upload(anyString(), any(List.class)))
            .willReturn(failedDocumentManagementUpload());

        ClaimData claimData = SampleClaimData.builder()
            .withAmount(SampleAmountRange.validDefaults())
            .build();

        makeRequest(claimData)
            .andExpect(status().isInternalServerError());
        
        verify(documentUploadClientApi).upload(anyString(), any(List.class));
    }

    @Test
    public void shouldNotifyStaffWithDocumentStoreClaimOnLegalClaimIssuedEvent() throws Exception {
        ClaimData claimData = SampleClaimData.builder()
            .withAmount(SampleAmountRange.validDefaults())
            .build();

        makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        verify(documentMetadataDownloadApi).getDocumentMetadata(anyString(), any(String.class));
        verify(documentDownloadClientApi).downloadBinary(anyString(), any(String.class));
    }

    private ResultActions makeRequest(ClaimData claimData) throws Exception {
        return webClient
            .perform(post("/claims/" + (Long) 123L)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(claimData))
            );
    }
}
