package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=http://core-case-data-api"
    }
)
public class LinkDefendantToClaimWithCoreCaseTest extends BaseIntegrationTest {

    @Before
    public void init() {
        given(userService.generatePin("John Smith", AUTHORISATION_TOKEN))
            .willReturn(new GeneratePinResponse("my-pin", "2"));

        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(PDF_BYTES);
    }

    @Test
    public void shouldReturn200HttpStatusAndUpdatedClaimWhenLinkIsSuccessfullySet() throws Exception {
        UserDetails claimantDetails = SampleUserDetails.getDefault();
        User claimant = SampleUser.builder()
            .withAuthorisation(AUTHORISATION_TOKEN)
            .withUserDetails(claimantDetails)
            .build();
        given(userService.getUserDetails(anyString())).willReturn(claimantDetails);

        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(claimant);

        UUID externalId = UUID.randomUUID();
        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder().withExternalId(externalId).build();

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.externalId", claimData.getExternalId().toString()))
            )
        ).willReturn(Collections.emptyList());

        given(coreCaseDataApi.startForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(EVENT_ID)
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(successfulCoreCaseDataStoreSubmitResponse());

        UserDetails defendantDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withRoles("citizen", "letter-" + SampleClaim.LETTER_HOLDER_ID)
            .build();

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult prepayment = makeRequestPrePayment(externalId.toString())
            .andExpect(status().isOk())
            .andReturn();

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

//        User defendant = SampleUser.builder()
//            .withAuthorisation(BEARER_TOKEN)
//            .withUserDetails(defendantDetails)
//            .build();
//
//        given(userService.getUser(BEARER_TOKEN)).willReturn(defendant);


//        webClient
//            .perform(put("/claims/defendant/link")
//                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
//            .andExpect(status().isOk());

        Claim claim = deserializeObjectFrom(result, Claim.class);

        assertThat(claimStore.getClaim(claim.getId()))
            .extracting(Claim::getId, Claim::getDefendantId)
            .containsExactly(claim.getId(), "555");
    }
}
