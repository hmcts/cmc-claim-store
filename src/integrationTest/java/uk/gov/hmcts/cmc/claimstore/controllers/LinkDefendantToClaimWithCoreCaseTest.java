package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LINK_DEFENDANT;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.listOfCaseDetails;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=http://core-case-data-api"
    }
)
public class LinkDefendantToClaimWithCoreCaseTest extends BaseIntegrationTest {

    public static final String ANONYMOUS_BEARER_TOKEN = "Anonymous Bearer token";
    public static final String ANONYMOUS_USER_ID = "3";

    @Before
    public void init() {
        given(userService.generatePin("John Smith", AUTHORISATION_TOKEN))
            .willReturn(new GeneratePinResponse("my-pin", "2"));

        given(userService.authenticateAnonymousCaseWorker())
            .willReturn(new User(ANONYMOUS_BEARER_TOKEN,
                SampleUserDetails.builder().withUserId(ANONYMOUS_USER_ID).build()));

        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(PDF_BYTES);
    }

    @Test
    public void shouldReturn200HttpStatusAndUpdatedClaimWhenLinkIsSuccessfullySet() throws Exception {
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
        ).thenReturn(listOfCaseDetails());

        UserDetails defendantDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withRoles("citizen", "letter-" + SampleClaim.LETTER_HOLDER_ID)
            .build();

        User defendant = SampleUser.builder()
            .withAuthorisation(BEARER_TOKEN)
            .withUserDetails(defendantDetails)
            .build();

        given(userService.getUser(eq(BEARER_TOKEN))).willReturn(defendant);
        given(userService.getUserDetails(eq(BEARER_TOKEN))).willReturn(defendant.getUserDetails());

        given(coreCaseDataApi.startEventForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(DEFENDANT_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq("1516189555935242"),
            eq(LINK_DEFENDANT.getValue())
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
        ).willReturn(successfulCoreCaseDataStoreSubmitResponse());

        given(caseAccessApi
            .findCaseIdsGivenUserIdHasAccessTo(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            )
        ).willReturn(ImmutableList.of("1516189555935242"));

        webClient
            .perform(put("/claims/defendant/link")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isOk());


        verify(jobService, atLeast(2)).scheduleJob(any(), any());
    }
}
