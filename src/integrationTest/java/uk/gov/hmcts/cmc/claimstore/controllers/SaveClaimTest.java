package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.service.notify.NotificationClientException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SaveClaimTest extends BaseIntegrationTest {

    private static final String REPRESENTATIVE_EMAIL_TEMPLATE = "f2b21b9c-fc4a-4589-807b-3156dbf5bf01";

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Before
    public void setup() {
        given(userService.getUserDetails("token"))
            .willReturn(SampleUserDetails.builder().withUserId(1L).withMail("claimant@email.com").build());

        given(userService.generatePin("John Smith", "token"))
            .willReturn(new GeneratePinResponse("my-pin", 2L));

        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});
    }

    @Test
    public void shouldReturnNewlyCreatedClaim() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);
    }

    @Test
    public void shouldFailWhenDuplicateExternalId() throws Exception {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.builder().withExternalId(externalId).build();
        claimStore.saveClaim(claimData);

        makeRequest(claimData)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldFailWhenStaffNotificationFails() throws Exception {
        doThrow(new RuntimeException("Sending failed"))
            .when(emailService).sendEmail(anyString(), any(EmailData.class));

        makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldRetrySendNotifications() throws Exception {
        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email1")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email2")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email3")));

        makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk());

        verify(notificationClient, atLeast(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        ClaimData claimData = SampleClaimData.builder()
            .withAmount(SampleAmountRange.validDefaults())
            .build();

        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(null);

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = deserializeObjectFrom(result, Claim.class);

        verify(notificationClient).sendEmail(eq(REPRESENTATIVE_EMAIL_TEMPLATE), anyString(),
            anyMap(), eq("representative-issue-notification-" + savedClaim.getReferenceNumber()));
    }

    @Test
    public void shouldSendStaffNotificationsForLegalClaimIssuedEvent() throws Exception {
        ClaimData claimData = SampleClaimData.builder()
            .withAmount(SampleAmountRange.validDefaults())
            .build();

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = deserializeObjectFrom(result, Claim.class);

        verify(emailService).sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getTo()).isEqualTo("recipient@example.com");
        assertThat(emailDataArgument.getValue().getSubject()).isEqualTo("Claim form " + savedClaim.getReferenceNumber());
        assertThat(emailDataArgument.getValue().getMessage()).isEqualTo("Please find attached claim.");
        assertThat(emailDataArgument.getValue().getAttachments()).hasSize(1)
            .first().extracting(EmailAttachment::getFilename)
            .containsExactly(savedClaim.getReferenceNumber() + "-sealed-claim.pdf");
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
