package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.claimstore.events.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails.getDefault;

public class StoreDefendantResponseTest extends BaseTest {

    private static final long RESPONSE_ID = 1L;
    private static final long CLAIM_ID = 2L;
    private static final long DEFENDANT_ID = 123L;

    private final DefendantResponse defendantResponse = SampleDefendantResponse.getDefault();

    @MockBean
    private DefendantResponseStaffNotificationHandler staffActionsHandler;

    @Captor
    private ArgumentCaptor<DefendantResponseEvent> defendantResponseEventArgument;

    @Before
    public void setup() {
        given(defendantResponseRepository.save(anyLong(), anyLong(), anyString(), anyString()))
            .willReturn(RESPONSE_ID);

        given(defendantResponseRepository.getById(RESPONSE_ID))
            .willReturn(Optional.of(defendantResponse));

        given(claimRepository.saveRepresented(
            anyString(), anyLong(), any(LocalDate.class), any(LocalDate.class), anyString(), anyString())
        ).willReturn(CLAIM_ID);

        given(claimRepository.getById(CLAIM_ID))
            .willReturn(Optional.of(claimAfterSaving));

        given(userService.getUserDetails(anyString())).willReturn(getDefault());
    }

    @Test
    public void shouldReturnNewlyCreatedDefendantResponse() throws Exception {
        //when
        final MvcResult result = postDefence(SampleResponseData.validDefaults())
            .andExpect(status().isOk())
            .andReturn();

        //then
        final DefendantResponse output =
            jsonMapper.fromJson(result.getResponse().getContentAsString(), DefendantResponse.class);

        assertThat(output).isEqualTo(defendantResponse);
    }

    @Test
    public void shouldInvokeStaffActionsHandlerAfterSuccessfulSave() throws Exception {
        postDefence(SampleResponseData.validDefaults())
            .andExpect(status().isOk())
            .andReturn();

        verify(staffActionsHandler).onDefendantResponseSubmitted(defendantResponseEventArgument.capture());
        assertThat(defendantResponseEventArgument.getValue().getClaim()).isEqualTo(claimAfterSaving);
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {

        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(null);

        postDefence(SampleResponseData.validDefaults())
            .andExpect(status().isOk())
            .andReturn();

        verify(notificationClient, times(2))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldReturnInternalServerErrorWhenStaffNotificationFails() throws Exception {
        doThrow(new RuntimeException()).when(staffActionsHandler).onDefendantResponseSubmitted(any());

        postDefence(SampleResponseData.validDefaults())
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    @Test
    public void shouldFailAWhenDefendantResponseFailedStoring() throws Exception {
        //when
        given(defendantResponseRepository.save(anyLong(), anyLong(), anyString(), anyString()))
            .willThrow(new DataAccessResourceFailureException("some failure"));

        final MvcResult result = postDefence(SampleResponseData.validDefaults())
            .andExpect(status().isInternalServerError())
            .andReturn();

        //then
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Internal server error");
    }

    @Test
    public void shouldFailForInvalidDefendantResponse() throws Exception {
        final MvcResult result = postDefence(SampleResponseData.builder()
                .withResponseType(null)
                .withMediation(null)
                .withDefence(null)
            .build())
            .andExpect(status().isBadRequest())
            .andReturn();

        final List<String> errors = extractErrors(result);

        assertThat(errors).hasSize(3).contains("type : may not be null",
            "freeMediation : may not be null",
            "defence : may not be empty");
    }

    @Test
    public void shouldFailForExceedingDefenceSizeLimit() throws Exception {
        final String defence = new ResourceReader().read("/defence_exceeding_size_limit.text");

        final MvcResult result = postDefence(SampleResponseData.builder()
                .withDefence(defence)
            .build())
            .andExpect(status().isBadRequest())
            .andReturn();

        final List<String> errors = extractErrors(result);
        assertThat(errors).hasSize(1).contains("defence : size must be between 0 and 99000");
    }

    @Test
    public void shouldFailForEmptyDefence() throws Exception {
        final MvcResult result = postDefence(SampleResponseData.builder()
                .withDefence("")
            .build())
            .andExpect(status().isBadRequest())
            .andReturn();

        final List<String> errors = extractErrors(result);
        assertThat(errors).hasSize(1).contains("defence : may not be empty");
    }

    private ResultActions postDefence(ResponseData responseData) throws Exception {
        return webClient
            .perform(post("/responses/claim/" + CLAIM_ID + "/defendant/" + DEFENDANT_ID)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(responseData))
            );
    }

}
