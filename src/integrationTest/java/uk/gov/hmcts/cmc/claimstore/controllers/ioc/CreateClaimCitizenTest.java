package uk.gov.hmcts.cmc.claimstore.controllers.ioc;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_CITIZEN_PAYMENT;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;

@TestPropertySource(
    properties = {
        "document_management.url=false",
        "core_case_data.api.url=http://core-case-data-api"
    }
)
public class CreateClaimCitizenTest extends BaseIntegrationTest {
    private static final Long CASE_ID = 42L;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;
    @Autowired
    private CaseMapper caseMapper;

    @Before
    public void before() {
        UserDetails userDetails = SampleUserDetails.builder()
            .withRoles("citizen")
            .withUserId(SUBMITTER_ID)
            .build();

        User user = new User(BEARER_TOKEN, userDetails);
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(userDetails);
        given(userService.getUser(BEARER_TOKEN)).willReturn(user);
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnCreatedClaim() throws Exception {
        UUID externalId = UUID.randomUUID();
        ClaimData claimData = SampleClaimData.submittedByClaimant()
            .toBuilder()
            .externalId(externalId)
            .payment(Payment.builder()
                .amount(new BigDecimal("10.00"))
                .reference("reference")
                .status(SUCCESS)
                .dateCreated("2017-12-03")
                .nextUrl("http://nexturl.test")
                .build())
            .build();
        Claim claim = SampleClaim.getDefault()
            .toBuilder()
            .externalId(externalId.toString())
            .submitterId(SUBMITTER_ID)
            .response(null)
            .claimData(claimData)
            .build();
        mockCcdCallsFor(claim);

        MvcResult result = makeRequest(claim.getClaimData(), BEARER_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
        Claim returnedClaim = deserializeObjectFrom(result, Claim.class);
        assertThat(returnedClaim.getExternalId()).isEqualTo(claim.getExternalId());
        assertThat(returnedClaim.getSubmitterId()).isEqualTo(claim.getSubmitterId());
        assertThat(returnedClaim.getClaimData()).isEqualTo(claimData);
    }

    private void mockCcdCallsFor(Claim claim) {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .state(AWAITING_CITIZEN_PAYMENT.getValue())
            .data(caseDetailsConverter.convertToMap(caseMapper.to(claim)))
            .build();

        when(coreCaseDataApi.searchForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(SUBMITTER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(searchCriteria(claim.getExternalId()))
            )
        ).thenReturn(ImmutableList.of(caseDetails));

        given(coreCaseDataApi.startEventForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(SUBMITTER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(String.valueOf(CASE_ID)),
            eq(CREATE_CITIZEN_CLAIM.getValue())
            )
        ).willReturn(StartEventResponse.builder()
            .eventId(CREATE_CITIZEN_CLAIM.getValue())
            .caseDetails(caseDetails)
            .token("atoken")
            .build());

        given(coreCaseDataApi.submitEventForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(SUBMITTER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(String.valueOf(CASE_ID)),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(caseDetails);
    }

    private ResultActions makeRequest(ClaimData claimData, String authorization) throws Exception {
        return webClient
            .perform(put("/claims/create-citizen-claim")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .content(jsonMapper.toJson(claimData))
            );
    }
}
