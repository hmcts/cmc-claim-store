package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.scheduler.model.JobData;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED_ONLINE;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.listOfCaseDetailsWithLinkedDefendant;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponseWithMoreTimeExtension;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "feature_toggles.reminderEmails=true"
    }
)
public class RequestMoreTimeForResponseWithCoreCaseDataTest extends BaseIntegrationTest {

    private static final String DEFENDANT_ID = "100";

    @Before
    public void before() {
        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail("defendant@example.com")
            .build();

        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, userDetails));
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(userDetails);
    }

    @Test
    public void shouldUpdatedResponseDeadlineWhenEverythingIsOk() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder().build();

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        when(coreCaseDataApi.searchForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(DEFENDANT_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(searchCriteria(claimData.getExternalId().toString()))
            )
        ).thenReturn(listOfCaseDetailsWithLinkedDefendant());

        given(coreCaseDataApi.startEventForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(DEFENDANT_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq("1516189555935242"),
            eq(MORE_TIME_REQUESTED_ONLINE.getValue())
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitEventForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(DEFENDANT_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq("1516189555935242"),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(successfulCoreCaseDataStoreSubmitResponseWithMoreTimeExtension());

        MvcResult result = makeRequest(claimData.getExternalId().toString())
            .andExpect(status().isOk())
            .andReturn();

        Claim claim = deserializeObjectFrom(result, Claim.class);

        verify(notificationClient, times(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());

        LocalDate responseDeadline = claim.getResponseDeadline();
        ZonedDateTime lastReminderDate = responseDeadline.minusDays(1).atTime(8, 0).atZone(UTC);
        ZonedDateTime firstReminderDate = responseDeadline.minusDays(5).atTime(8, 0).atZone(UTC);

        verify(jobService).rescheduleJob(any(JobData.class), eq(firstReminderDate));
        verify(jobService).rescheduleJob(any(JobData.class), eq(lastReminderDate));
    }

    private ResultActions makeRequest(String externalId) throws Exception {
        return makeRequest(externalId, Maps.newHashMap(HttpHeaders.AUTHORIZATION, BEARER_TOKEN));
    }

    private ResultActions makeRequest(String externalId, Map<String, String> headers) throws Exception {
        MockHttpServletRequestBuilder builder = post("/claims/" + externalId + "/request-more-time");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        return webClient.perform(builder);
    }
}
