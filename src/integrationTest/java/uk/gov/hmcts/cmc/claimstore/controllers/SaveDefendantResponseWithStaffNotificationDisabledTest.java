package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailData;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails.getDefault;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.RESPONSE_DEADLINE;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=false",
        "feature_toggles.emailToStaff=false"
    }
)
public class SaveDefendantResponseWithStaffNotificationDisabledTest extends BaseIntegrationTest {
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Before
    public void setup() {
        given(userService.getUserDetails(anyString())).willReturn(getDefault());
    }

    @Test
    public void shouldNotInvokeStaffActionsHandlerAfterSuccessfulSave() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);
        Response response = SampleResponse.validDefaults();

        makeRequest(claim.getExternalId(), DEFENDANT_ID, response)
            .andExpect(status().isOk());

        verify(emailService, never()).sendEmail(eq("sender@example.com"), emailDataArgument.capture());
    }

    @Test
    public void shouldNotSendStaffNotificationsForDefendantRequestedMoreTimeEvent() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);

        LocalDate newDeadline = RESPONSE_DEADLINE.plusDays(20);
        Claim updated = SampleClaim.builder().withResponseDeadline(newDeadline).withClaimData(claim.getClaimData())
            .withReferenceNumber(claim.getReferenceNumber())
            .withExternalId(claim.getExternalId()).build();

        makeRequest(claim.getExternalId(), updated)
            .andExpect(status().isOk());

        verify(emailService, never()).sendEmail(eq("sender@example.com"), emailDataArgument.capture());
    }

    private ResultActions makeRequest(String externalId, String defendantId, Response response) throws Exception {
        return webClient
            .perform(post("/responses/claim/" + externalId + "/defendant/" + defendantId)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(response))
            );
    }

    private ResultActions makeRequest(String externalId, Claim claim) throws Exception {
        return webClient
            .perform(post("/claims/" + externalId + "/request-more-time")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(claim))
            );
    }
}
