package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.DocumentManagementBaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "feature_toggles.document_management=true"
    }
)
public class ResendStaffNotificationsTest extends DocumentManagementBaseIntegrationTest {

    @Test
    public void shouldResendStaffNotificationWithDocumentStoreClaimOnClaimIssuedEvent() throws Exception {
        final String event = "claim-issued";

        Claim claim = claimStore.saveClaim(SampleClaimData.submittedByClaimant());

        final GeneratePinResponse pinResponse = new GeneratePinResponse("pin-123", "333");
        given(userService.generatePin(anyString(), eq("ABC123"))).willReturn(pinResponse);
        given(userService.getUserDetails(anyString())).willReturn(SampleUserDetails.getDefault());

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isOk());

        verify(documentUploadClientApi).upload(anyString(), any(List.class));
        verify(documentMetadataDownloadApi).getDocumentMetadata(anyString(), any(String.class));
        verify(documentDownloadClientApi).downloadBinary(anyString(), any(String.class));
    }


    private ResultActions makeRequest(String referenceNumber, String event) throws Exception {
        return webClient
            .perform(put("/support/claim/" + referenceNumber + "/event/" + event + "/resend-staff-notifications")
                .header(HttpHeaders.AUTHORIZATION, "ABC123"));
    }
}
