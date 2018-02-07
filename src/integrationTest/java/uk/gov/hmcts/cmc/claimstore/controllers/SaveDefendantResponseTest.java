package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails.getDefault;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class SaveDefendantResponseTest extends BaseIntegrationTest {

    @MockBean
    private DefendantResponseStaffNotificationHandler staffActionsHandler;

    @Captor
    private ArgumentCaptor<DefendantResponseEvent> defendantResponseEventArgument;

    @Before
    public void setup() {
        given(userService.getUserDetails(anyString())).willReturn(getDefault());
    }

    @Test
    public void shouldReturnNewlyCreatedDefendantResponse() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);
        Response response = SampleResponse.validDefaults();

        MvcResult result = makeRequest(claim.getExternalId(), DEFENDANT_ID, response)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getResponse, Claim::getRespondedAt)
            .doesNotContainNull()
            .contains(Optional.of(response));
    }

    @Test
    public void shouldInvokeStaffActionsHandlerAfterSuccessfulSave() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);
        Response response = SampleResponse.validDefaults();

        makeRequest(claim.getExternalId(), DEFENDANT_ID, response)
            .andExpect(status().isOk());

        verify(staffActionsHandler).onDefendantResponseSubmitted(defendantResponseEventArgument.capture());

        Claim updatedClaim = claimRepository.getById(claim.getId()).orElseThrow(RuntimeException::new);
        assertThat(defendantResponseEventArgument.getValue().getClaim()).isEqualTo(updatedClaim);
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);
        Response response = SampleResponse.validDefaults();

        makeRequest(claim.getExternalId(), DEFENDANT_ID, response)
            .andExpect(status().isOk());

        verify(notificationClient, times(2))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldReturnInternalServerErrorWhenStaffNotificationFails() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);
        Response response = SampleResponse.validDefaults();

        doThrow(new RuntimeException()).when(staffActionsHandler).onDefendantResponseSubmitted(any());

        makeRequest(claim.getExternalId(), DEFENDANT_ID, response)
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldFailForEmptyDefence() throws Exception {
        String anyExternalId = "84f1dda3-e205-4277-96a6-1f23b6f1766d";
        String anyDefendantId = "500";
        Response response = SampleResponse.FullDefence.builder()
            .withDefence("")
            .build();

        MvcResult result = makeRequest(anyExternalId, anyDefendantId, response)
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(extractErrors(result))
            .hasSize(1)
            .contains("defence : may not be empty");
    }

    private ResultActions makeRequest(String externalId, String defendantId, Response response) throws Exception {
        return webClient
            .perform(post("/responses/claim/" + externalId + "/defendant/" + defendantId)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(response))
            );
    }
}
