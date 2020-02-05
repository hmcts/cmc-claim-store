package uk.gov.hmcts.cmc.claimstore.controllers.support;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LINK_LETTER_HOLDER;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.listOfCaseDetails;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.listOfCaseDetailsWithCCJ;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.listOfCaseDetailsWithDefResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.listOfCaseDetailsWithDefendant;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.listOfCaseDetailsWithOfferCounterSigned;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitRepresentativeResponse;

public class ResendStaffNotificationsCoreCaseDataTest extends BaseMockSpringTest {

    private static final String CASE_REFERENCE = "000MC023";
    private static final String PAGE = "1";

    @MockBean
    protected EmailService emailService;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Before
    public void setUp() {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});
        UserDetails userDetails = SampleUserDetails.builder().withRoles("caseworker-cmc").build();
        User user = new User(BEARER_TOKEN, userDetails);
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(userDetails);
        given(userService.authenticateAnonymousCaseWorker()).willReturn(user);
        given(userService.getUser(BEARER_TOKEN)).willReturn(user);
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldRespond404WhenClaimDoesNotExist() throws Exception {
        String nonExistingCaseReference = "something";

        givenSearchByReferenceNumberReturns(nonExistingCaseReference, Collections.emptyList());

        makeRequest(nonExistingCaseReference, "claim").andExpect(status().isNotFound());

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldRespond404WhenEventIsNotSupported() throws Exception {
        givenSearchByReferenceNumberReturns(CASE_REFERENCE, listOfCaseDetails());

        makeRequest(CASE_REFERENCE, "non-existing-event").andExpect(status().isNotFound());

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldRespond409AndNotProceedForClaimIssuedEventWhenClaimIsLinkedToDefendant() throws Exception {
        givenSearchByReferenceNumberReturns(CASE_REFERENCE, listOfCaseDetailsWithDefendant());

        makeRequest(CASE_REFERENCE, "claim").andExpect(status().isConflict());

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    @Ignore
    public void shouldRespond200AndSendNotificationsForClaimIssuedEvent() throws Exception {
        givenSearchByReferenceNumberReturns(CASE_REFERENCE, listOfCaseDetails());
        given(userService.generatePin(anyString(), eq(BEARER_TOKEN)))
            .willReturn(new GeneratePinResponse("pin-123", "333"));
        given(sendLetterApi.sendLetter(anyString(), any(Letter.class)))
            .willReturn(new SendLetterResponse(UUID.randomUUID()));

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        given(coreCaseDataApi.startEventForCaseWorker(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            anyString(),
            eq(LINK_LETTER_HOLDER.getValue())
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitEventForCaseWorker(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            anyString(),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(successfulCoreCaseDataStoreSubmitRepresentativeResponse());

        makeRequest(CASE_REFERENCE, "claim").andExpect(status().isOk());

        verify(emailService, times(2)).sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        EmailData emailData = emailDataArgument.getValue();
        assertThat(emailData.getTo()).isEqualTo("recipient@example.com");
        assertThat(emailData.getSubject()).isEqualTo("Claim " + CASE_REFERENCE + " issued");
        assertThat(emailData.getMessage()).isEqualTo("Please find attached claim.");

        verify(coreCaseDataApi)
            .startEventForCaseWorker(
                eq(BEARER_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                anyString(),
                eq(LINK_LETTER_HOLDER.getValue())
            );

        verify(coreCaseDataApi)
            .submitEventForCaseWorker(
                eq(BEARER_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                anyString(),
                eq(IGNORE_WARNING),
                any()
            );
    }

    @Test
    public void shouldRespond409AndNotProceedForMoreTimeRequestedEventWhenMoreTimeNotRequested() throws Exception {
        givenSearchByReferenceNumberReturns(CASE_REFERENCE, listOfCaseDetails());

        makeRequest(CASE_REFERENCE, "more-time").andExpect(status().isConflict());

        verify(notificationClient, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForMoreTimeRequestedEvent() throws Exception {
        givenSearchByReferenceNumberReturns(CASE_REFERENCE, listOfCaseDetailsWithDefendant());

        makeRequest(CASE_REFERENCE, "more-time").andExpect(status().isOk());

        verify(notificationClient).sendEmail(eq("staff-more-time-requested-template"),
            eq("recipient@example.com"),
            any(),
            eq("more-time-requested-notification-to-staff-" + CASE_REFERENCE));
    }

    @Test
    public void shouldRespond409AndNotProceedForResponseSubmittedEventWhenResponseNotSubmitted() throws Exception {
        givenSearchByReferenceNumberReturns(CASE_REFERENCE, listOfCaseDetails());

        makeRequest(CASE_REFERENCE, "response").andExpect(status().isConflict());

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForResponseSubmittedEvent() throws Exception {
        givenSearchByReferenceNumberReturns(CASE_REFERENCE, listOfCaseDetailsWithDefResponse());

        makeRequest(CASE_REFERENCE, "response").andExpect(status().isOk());

        verify(emailService).sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getTo()).isEqualTo("recipient@example.com");
        assertThat(emailDataArgument.getValue().getSubject())
            .isEqualTo("Civil Money Claim full admission submitted: Pay by instalments "
                + CASE_REFERENCE
                + " - Jan Clark v Mary Richards");
        assertThat(emailDataArgument.getValue().getMessage()).contains(
            "Defendant's details can be found on the attached defence copy. Additional contact details are:",
            "Email: j.smith@example.com",
            "Phone number: 07873727165"
        );
    }

    @Test
    @Ignore
    public void shouldRespond200AndSendNotificationsForCCJRequestedEvent() throws Exception {
        givenSearchByReferenceNumberReturns(CASE_REFERENCE, listOfCaseDetailsWithCCJ());

        makeRequest(CASE_REFERENCE, "ccj").andExpect(status().isOk());

        verify(emailService).sendEmail(eq("sender@example.com"), emailDataArgument.capture());
    }

    @Test
    @Ignore
    public void shouldRespond200AndSendNotificationsForOfferAcceptedEvent() throws Exception {
        givenSearchByReferenceNumberReturns(CASE_REFERENCE, listOfCaseDetailsWithOfferCounterSigned());

        makeRequest(CASE_REFERENCE, "settlement").andExpect(status().isOk());

        verify(emailService).sendEmail(eq("sender@example.com"), emailDataArgument.capture());
    }

    private void givenSearchByReferenceNumberReturns(String caseReference, List<CaseDetails> cases) {
        given(coreCaseDataApi.searchForCaseworker(
            any(),
            any(),
            any(),
            any(),
            any(),
            eq(ImmutableMap.of("case.previousServiceCaseReference", caseReference,
                "page", PAGE,
                "sortDirection", "desc"))
            )
        ).willReturn(cases);
    }

    private ResultActions makeRequest(String referenceNumber, String event) throws Exception {
        return webClient
            .perform(put("/support/claim/" + referenceNumber + "/event/" + event + "/resend-staff-notifications")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN));
    }
}
