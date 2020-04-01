package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CountyCourtJudgmentTest extends BaseMockSpringTest {
    private static final String ROOT_URL = "/claims";
    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withUserId(SampleClaim.USER_ID)
        .withMail(SampleClaim.SUBMITTER_EMAIL)
        .withRoles(Role.CITIZEN.getRole())
        .build();
    private static final User USER = SampleUser.builder()
        .withUserDetails(USER_DETAILS)
        .withAuthorisation(BEARER_TOKEN)
        .build();

    @MockBean
    protected EmailService emailService;

    @MockBean
    protected CaseRepository caseRepository;

    @Before
    public void setUp() {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);
        given(userService.getUser(BEARER_TOKEN)).willReturn(USER);
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void testSave() throws Exception {
        Claim claim = SampleClaim.getDefault().toBuilder()
            .countyCourtJudgment(null)
            .countyCourtJudgmentRequestedAt(null)
            .build();
        CountyCourtJudgment ccj = CountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.ADMISSIONS)
            .paymentOption(PaymentOption.IMMEDIATELY).build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));

        Claim result = jsonMappingHelper.deserializeObjectFrom(
            doPost(BEARER_TOKEN, ccj,
                ROOT_URL + "/{externalId}/county-court-judgment", SampleClaim.EXTERNAL_ID)
                .andExpect(status().isOk())
                .andReturn(),
            Claim.class);

        verify(caseRepository).saveCountyCourtJudgment(BEARER_TOKEN, claim, ccj);
        verify(eventProducer).createCountyCourtJudgmentEvent(claim, BEARER_TOKEN);
        verify(caseRepository, never()).saveCaseEvent(anyString(), any(Claim.class), eq(CaseEvent.LIFT_STAY));

        assertThat(result).isNotNull();
    }

    @Test
    public void testSaveMissingClaim() throws Exception {
        CountyCourtJudgment ccj = CountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.ADMISSIONS)
            .paymentOption(PaymentOption.IMMEDIATELY).build();

        when(caseRepository.getClaimByExternalId(anyString(), any(User.class)))
            .thenReturn(Optional.empty());

        doPost(BEARER_TOKEN, ccj,
            ROOT_URL + "/{externalId}/county-court-judgment", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testSaveCCJAlreadyRequested() throws Exception {
        Claim claim = SampleClaim.getDefault(); // this has a CCJ
        CountyCourtJudgment ccj = CountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.ADMISSIONS)
            .paymentOption(PaymentOption.IMMEDIATELY).build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));

        doPost(BEARER_TOKEN, ccj,
            ROOT_URL + "/{externalId}/county-court-judgment", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isForbidden());
    }

    @Test
    public void testSaveCCJAutomaticallyLiftsStay() throws Exception {
        Claim claim = SampleClaim.getDefault().toBuilder()
            .countyCourtJudgment(null)
            .countyCourtJudgmentRequestedAt(null)
            .state(ClaimState.STAYED)
            .response(SampleResponse.FullAdmission.builder().build())
            .build();
        CountyCourtJudgment ccj = CountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.ADMISSIONS)
            .paymentOption(PaymentOption.IMMEDIATELY).build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));
        when(caseRepository.saveCaseEvent(BEARER_TOKEN, claim, CaseEvent.LIFT_STAY))
            .thenReturn(claim);

        doPost(BEARER_TOKEN, ccj,
            ROOT_URL + "/{externalId}/county-court-judgment", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isOk());

        verify(caseRepository).saveCaseEvent(BEARER_TOKEN, claim, CaseEvent.LIFT_STAY);
    }

    @Test
    public void testRedetermination() throws Exception {
        Claim claim = SampleClaim.getDefault().toBuilder()
            .reDetermination(null)
            .reDeterminationRequestedAt(null)
            .build();
        ReDetermination redetermination = ReDetermination.builder()
            .explanation("blah".repeat(4))
            .partyType(MadeBy.CLAIMANT).build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));

        Claim result = jsonMappingHelper.deserializeObjectFrom(
            doPost(BEARER_TOKEN, redetermination,
                ROOT_URL + "/{externalId}/re-determination", SampleClaim.EXTERNAL_ID)
                .andExpect(status().isOk())
                .andReturn(),
            Claim.class);

        assertThat(result)
            .isNotNull();

        verify(eventProducer)
            .createRedeterminationEvent(claim, BEARER_TOKEN, USER_DETAILS.getFullName(), MadeBy.CLAIMANT);
    }

    @Test
    public void testRedeterminationMissingClaim() throws Exception {
        ReDetermination redetermination = ReDetermination.builder()
            .explanation("blah".repeat(4))
            .partyType(MadeBy.CLAIMANT).build();

        when(caseRepository.getClaimByExternalId(anyString(), any(User.class)))
            .thenReturn(Optional.empty());

        doPost(BEARER_TOKEN, redetermination,
            ROOT_URL + "/{externalId}/re-determination", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testRedeterminationNotByParticipant() throws Exception {
        Claim claim = SampleClaim.getDefault().toBuilder()
            .submitterId(SampleClaim.DEFENDANT_ID)
            .build();
        ReDetermination redetermination = ReDetermination.builder()
            .explanation("blah".repeat(4))
            .partyType(MadeBy.CLAIMANT).build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));

        doPost(BEARER_TOKEN, redetermination,
            ROOT_URL + "/{externalId}/re-determination", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isForbidden());
    }

    @Test
    public void testRedeterminationWithoutCCJ() throws Exception {
        Claim claim = SampleClaim.getDefault().toBuilder()
            .countyCourtJudgment(null)
            .countyCourtJudgmentRequestedAt(null)
            .reDetermination(null)
            .reDeterminationRequestedAt(null)
            .build();
        ReDetermination redetermination = ReDetermination.builder()
            .explanation("blah".repeat(4))
            .partyType(MadeBy.CLAIMANT).build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));

        doPost(BEARER_TOKEN, redetermination,
            ROOT_URL + "/{externalId}/re-determination", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isForbidden());
    }

    @Test
    public void testRedeterminationAlreadyRequested() throws Exception {
        ReDetermination redetermination = ReDetermination.builder()
            .explanation("blah".repeat(4))
            .partyType(MadeBy.CLAIMANT).build();
        Claim claim = SampleClaim.getDefault().toBuilder()
            .countyCourtJudgment(CountyCourtJudgment.builder().build())
            .countyCourtJudgmentRequestedAt(LocalDateTime.now())
            .reDetermination(redetermination)
            .reDeterminationRequestedAt(LocalDateTime.now())
            .build();

        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));

        doPost(BEARER_TOKEN, redetermination,
            ROOT_URL + "/{externalId}/re-determination", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isForbidden());
    }
}
