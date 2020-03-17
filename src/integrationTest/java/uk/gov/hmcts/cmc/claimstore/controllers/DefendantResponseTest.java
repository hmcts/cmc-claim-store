package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailService;

import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantResponseTest extends BaseMockSpringTest {
    private static final String RESPONSE_URL = "/responses/claim/{externalId}/defendant/{defendantId}";

    private static final Claim CLAIM_OPEN = SampleClaim.getDefaultWithoutResponse(SampleClaim.DEFENDANT_EMAIL);
    private static final Claim CLAIM_LINKED = CLAIM_OPEN.toBuilder()
        .defendantId(SampleClaim.DEFENDANT_ID)
        .build();
    private static final Claim CLAIM_WITH_CCJ = CLAIM_LINKED.toBuilder()
        .countyCourtJudgmentRequestedAt(now())
        .countyCourtJudgment(SampleCountyCourtJudgment.builder().build())
        .build();
    private static final Claim CLAIM_RESPONDED = CLAIM_LINKED.toBuilder()
        .respondedAt(now())
        .response(SampleResponse.validDefaults())
        .build();

    @MockBean
    protected EmailService emailService;

    @MockBean
    protected ClaimService claimService;

    @Before
    public void setup() {
        given(userService.getUserDetails(AUTHORISATION_TOKEN))
            .willReturn(SampleUserDetails.builder()
                .withUserId(SampleClaim.DEFENDANT_ID)
                .withMail(SampleClaim.DEFENDANT_EMAIL)
                .withRoles(Role.CITIZEN.getRole())
                .build());
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void testForbiddenWhenUnlinked() throws Exception {
        submitResponse(
            CLAIM_OPEN,
            SampleResponse.validDefaults()
        ).andExpect(
            status().isForbidden()
        );
        verify(claimService, never())
            .saveDefendantResponse(any(), any(), any(), any());
        verify(eventProducer, never())
            .createDefendantResponseEvent(any(), any());
    }

    @Test
    public void testHappyPathFullDefence() throws Exception {
        Response response = SampleResponse.FullDefence.builder().build();
        submitResponse(
            CLAIM_LINKED,
            response
        ).andExpect(
            status().isOk()
        );
        verify(claimService)
            .saveDefendantResponse(CLAIM_LINKED, SampleClaim.DEFENDANT_EMAIL, response, AUTHORISATION_TOKEN);
        verify(eventProducer)
            .createDefendantResponseEvent(CLAIM_LINKED, AUTHORISATION_TOKEN);
    }

    @Test
    public void testHappyPathPartAdmit() throws Exception {
        Response response = SampleResponse.PartAdmission.builder().build();
        submitResponse(
            CLAIM_LINKED,
            response
        ).andExpect(
            status().isOk()
        );
        verify(claimService)
            .saveDefendantResponse(CLAIM_LINKED, SampleClaim.DEFENDANT_EMAIL, response, AUTHORISATION_TOKEN);
        verify(eventProducer)
            .createDefendantResponseEvent(CLAIM_LINKED, AUTHORISATION_TOKEN);
    }

    @Test
    public void testHappyPathFullAdmit() throws Exception {
        Response response = SampleResponse.FullAdmission.builder().build();
        submitResponse(
            CLAIM_LINKED,
            response
        ).andExpect(
            status().isOk()
        );
        verify(claimService)
            .saveDefendantResponse(CLAIM_LINKED, SampleClaim.DEFENDANT_EMAIL, response, AUTHORISATION_TOKEN);
        verify(eventProducer)
            .createDefendantResponseEvent(CLAIM_LINKED, AUTHORISATION_TOKEN);
    }

    @Test
    public void testForbiddenWhenAlreadyResponded() throws Exception {
        submitResponse(
            CLAIM_RESPONDED,
            SampleResponse.validDefaults()
        ).andExpect(
            status().isForbidden()
        );
        verify(claimService, never())
            .saveDefendantResponse(any(), any(), any(), any());
        verify(eventProducer, never())
            .createDefendantResponseEvent(any(), any());
    }

    @Test
    public void testForbiddenWhenCCJRequested() throws Exception {
        submitResponse(
            CLAIM_WITH_CCJ,
            SampleResponse.validDefaults()
        ).andExpect(
            status().isForbidden()
        );
        verify(claimService, never())
            .saveDefendantResponse(any(), any(), any(), any());
        verify(eventProducer, never())
            .createDefendantResponseEvent(any(), any());
    }

    private ResultActions submitResponse(Claim claim, Response response) throws Exception {
        when(claimService.getClaimByExternalId(claim.getExternalId(), AUTHORISATION_TOKEN))
            .thenReturn(claim);
        return webClient.perform(
            post(RESPONSE_URL, SampleClaim.EXTERNAL_ID, SampleClaim.DEFENDANT_ID)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMappingHelper.toJson(response)));
    }
}
