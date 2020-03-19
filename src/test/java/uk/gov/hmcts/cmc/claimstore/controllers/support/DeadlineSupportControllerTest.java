package uk.gov.hmcts.cmc.claimstore.controllers.support;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.utils.DateUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.YEARS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class DeadlineSupportControllerTest {
    private static final LocalDateTime PRE_5_0_0_DATETIME = DateUtils.DATE_OF_5_0_0_RELEASE.minus(1, YEARS);

    @Mock
    private UserService userService;
    @Mock
    private ClaimService claimService;
    @Mock
    private CaseRepository caseRepository;
    @Mock
    private DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;

    private DeadlineSupportController controller;

    @Before
    public void setUp() {
        controller = new DeadlineSupportController(userService,
            claimService,
            directionsQuestionnaireDeadlineCalculator,
            caseRepository
        );
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(new User("authorisation", null));
    }

    @Test(expected = NullPointerException.class)
    public void testNullUserService() {
        new DeadlineSupportController(null, claimService, directionsQuestionnaireDeadlineCalculator, caseRepository);
    }

    @Test(expected = NullPointerException.class)
    public void testNullClaimService() {
        new DeadlineSupportController(userService, null, directionsQuestionnaireDeadlineCalculator, caseRepository);
    }

    @Test(expected = NullPointerException.class)
    public void testNullDQCalculator() {
        new DeadlineSupportController(userService, claimService, null, caseRepository);
    }

    @Test(expected = NullPointerException.class)
    public void testNullCaseRepository() {
        new DeadlineSupportController(userService, claimService, directionsQuestionnaireDeadlineCalculator, null);
    }

    @Test(expected = NotFoundException.class)
    public void testDefineDeadlineOnMissingClaim() {
        controller.defineDeadline("dq", "000MC001", false);
    }

    @Test
    public void testDefineUnknownDeadline() {
        Claim sampleClaim = SampleClaim.getDefault();
        when(claimService.getClaimByReferenceAnonymous(sampleClaim.getReferenceNumber()))
            .thenReturn(Optional.of(sampleClaim));

        ResponseEntity<String> response = controller.defineDeadline("unknown", sampleClaim.getReferenceNumber(), false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isEqualTo("Unrecognised deadline type: unknown");
        verifyNoInteractions(directionsQuestionnaireDeadlineCalculator);
        verifyNoInteractions(caseRepository);
    }

    @Test
    public void testDefineDQDeadlineWhenAlreadyPresent() {
        final String reference = "000MC002";
        final LocalDate deadline = LocalDate.now();
        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withDirectionsQuestionnaireDeadline(deadline)
            .build()));

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
            .isEqualTo("Claim %s already has a directions questionnaire deadline of %s.",
                reference, deadline);
        verifyNoInteractions(directionsQuestionnaireDeadlineCalculator);
        verifyNoInteractions(caseRepository);
    }

    @Test
    public void testDefineDQDeadlineWhenAlreadyPresentWithOverwriteEnabled() {
        final String reference = "000MC002";
        final LocalDate deadline = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withCreatedAt(now)
            .withResponse(SampleResponse.validDefaults())
            .withClaimantRespondedAt(now)
            .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
            .withDirectionsQuestionnaireDeadline(deadline)
            .build()));

        when(directionsQuestionnaireDeadlineCalculator.calculateDirectionsQuestionnaireDeadline(eq(now)))
            .thenReturn(deadline);

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, true);

        verify(caseRepository, once()).updateDirectionsQuestionnaireDeadline(any(Claim.class), any(), anyString());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
            .isEqualTo("Claim %s has been been assigned a "
                + "directions questionnaire deadline of %s.", reference, deadline);
    }

    @Test
    public void testDefineDQDeadlineWhenDQFeatureEnabled() {
        final String reference = "000MC011";
        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withFeatures(Collections.singletonList("directionsQuestionnaire"))
            .build()));

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
            .isEqualTo("Claim %s has online DQs enabled; "
                + "cannot define a directions questionnaire deadline", reference);
        verifyNoInteractions(directionsQuestionnaireDeadlineCalculator);
        verifyNoInteractions(caseRepository);
    }

    @Test
    public void testDefineDQDeadlineWithNoResponse() {
        final String reference = "000MC003";
        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withResponse(null)
            .build()));

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
            .isEqualTo("Claim %s does not have a response; "
                + "cannot define a directions questionnaire deadline", reference);
        verifyNoInteractions(directionsQuestionnaireDeadlineCalculator);
        verifyNoInteractions(caseRepository);
    }

    @Test
    public void testDefineDQDeadlineOnPre500WithDefendantMediation() {
        final String reference = "000MC004";
        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withCreatedAt(PRE_5_0_0_DATETIME)
            .withResponse(SampleResponse.FullDefence.builder()
                .withMediation(YesNoOption.YES)
                .build())
            .build()));

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
            .isEqualTo("Claim %s is from before 5.0.0 and its defendant agreed to mediation; "
                + "cannot define a directions questionnaire deadline.", reference);
        verifyNoInteractions(directionsQuestionnaireDeadlineCalculator);
        verifyNoInteractions(caseRepository);
    }

    @Test
    public void testDefineDQDeadlineOnPre500WithFullAdmitResponse() {
        final String reference = "000MC005";
        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withCreatedAt(PRE_5_0_0_DATETIME)
            .withResponse(SampleResponse.FullAdmission.builder()
                .withMediation(YesNoOption.NO)
                .build())
            .build()));

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
            .isEqualTo("Claim %s is from before 5.0.0 and its defendant fully admitted; "
                + "cannot define a directions questionnaire deadline.", reference);
        verifyNoInteractions(directionsQuestionnaireDeadlineCalculator);
        verifyNoInteractions(caseRepository);
    }

    @Test
    public void testDefineDQDeadlineOnValidPre500Claim() {
        final String reference = "000MC006";
        final LocalDate deadline = LocalDate.now();
        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withCreatedAt(PRE_5_0_0_DATETIME)
            .withResponse(SampleResponse.FullDefence.builder()
                .withMediation(YesNoOption.NO)
                .build())
            .withRespondedAt(LocalDateTime.now())
            .build()));
        when(directionsQuestionnaireDeadlineCalculator
            .calculateDirectionsQuestionnaireDeadline(any(LocalDateTime.class))).thenReturn(deadline);

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, false);

        verify(caseRepository, once()).updateDirectionsQuestionnaireDeadline(any(Claim.class), eq(deadline),
            anyString());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
            .isEqualTo("Claim %s has been been assigned a "
                + "directions questionnaire deadline of %s.", reference, deadline);
    }

    @Test
    public void testDefineDQDeadlineOnPost500ClaimWithNoClaimantResponse() {
        final String reference = "000MC007";
        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withCreatedAt(LocalDateTime.now())
            .withResponse(SampleResponse.validDefaults())
            .withClaimantRespondedAt(null)
            .withClaimantResponse(null)
            .build()));

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
            .isEqualTo("Claim %s does not have a claimant response; "
                + "cannot define a directions questionnaire deadline", reference);
        verifyNoInteractions(directionsQuestionnaireDeadlineCalculator);
        verifyNoInteractions(caseRepository);
    }

    @Test
    public void testDefineDQDeadlineOnPost500ClaimWithClaimantAcceptation() {
        final String reference = "000MC008";
        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withCreatedAt(LocalDateTime.now())
            .withResponse(SampleResponse.validDefaults())
            .withClaimantRespondedAt(LocalDateTime.now())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .build()));

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
            .isEqualTo("Claim %s has an acceptation claimant response; "
                + "cannot define a directions questionnaire deadline.", reference);
        verifyNoInteractions(directionsQuestionnaireDeadlineCalculator);
        verifyNoInteractions(caseRepository);
    }

    @Test
    public void testDefineDQDeadlineOnPost500ClaimWithClaimantMediation() {
        final String reference = "000MC009";
        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withCreatedAt(LocalDateTime.now())
            .withResponse(SampleResponse.validDefaults())
            .withClaimantRespondedAt(LocalDateTime.now())
            .withClaimantResponse(SampleClaimantResponse.validRejectionWithFreeMediation())
            .build()));

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
            .isEqualTo("Claim %s has a mediation agreement; "
                + "cannot define a directions questionnaire deadline.", reference);
        verifyNoInteractions(directionsQuestionnaireDeadlineCalculator);
        verifyNoInteractions(caseRepository);
    }

    @Test
    public void testDefineDQDeadlineOnValidPost500Claim() {
        final String reference = "000MC010";
        final LocalDate deadline = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        when(claimService.getClaimByReferenceAnonymous(reference)).thenReturn(Optional.of(SampleClaim.builder()
            .withReferenceNumber(reference)
            .withCreatedAt(now)
            .withResponse(SampleResponse.validDefaults())
            .withClaimantRespondedAt(now)
            .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
            .build()));

        when(directionsQuestionnaireDeadlineCalculator.calculateDirectionsQuestionnaireDeadline(eq(now)))
            .thenReturn(deadline);

        ResponseEntity<String> response = controller.defineDeadline("dq", reference, false);

        verify(caseRepository, once()).updateDirectionsQuestionnaireDeadline(any(Claim.class), any(), anyString());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
            .isEqualTo("Claim %s has been been assigned a "
                + "directions questionnaire deadline of %s.", reference, deadline);
    }
}
