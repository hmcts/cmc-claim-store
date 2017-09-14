package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefaultJudgment;
import uk.gov.hmcts.cmc.claimstore.repositories.DefaultJudgmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJudgmentServiceTest {

    private static final long DEFAULT_JUDGMENT_ID = 33L;

    private DefaultJudgmentService defaultJudgmentService;

    @Mock
    private ClaimService claimService;
    @Mock
    private EventProducer eventProducer;
    @Mock
    private DefaultJudgmentRepository defaultJudgmentRepository;

    @Before
    public void setup() {
        defaultJudgmentService = new DefaultJudgmentService(
            claimService,
            defaultJudgmentRepository,
            eventProducer
        );
    }

    @Test
    public void getByIdShouldReturnDefaultJudgmentWhenDefaultJudgmentExists() {

        DefaultJudgment expected = getDefaultJudgmentModel();

        when(defaultJudgmentRepository.getByClaimId(eq(CLAIM_ID))).thenReturn(Optional.of(expected));

        DefaultJudgment actual = defaultJudgmentService.getByClaimId(CLAIM_ID);
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NotFoundException.class)
    public void getByIdShouldThrowExceptionWhenDefaultJudgmentDoesNotExist() {
        defaultJudgmentService.getByClaimId(CLAIM_ID);
    }

    @Test
    public void saveShouldFinishSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.getWithResponseDeadline(LocalDate.now().minusMonths(2));
        DefaultJudgment defaultJudgment = getDefaultJudgmentModel();

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(claim);
        when(defaultJudgmentRepository.getByClaimId(eq(CLAIM_ID))).thenReturn(Optional.empty());
        when(defaultJudgmentRepository.save(eq(CLAIM_ID), eq(USER_ID), anyString(), eq("{}")))
            .thenReturn(DEFAULT_JUDGMENT_ID);
        when(defaultJudgmentRepository.getById(eq(DEFAULT_JUDGMENT_ID))).thenReturn(Optional.of(defaultJudgment));

        defaultJudgmentService.save(USER_ID, "{}", CLAIM_ID);

        verify(eventProducer, once()).createDefaultJudgmentSubmittedEvent(eq(defaultJudgment), eq(claim));
    }

    @Test(expected = NotFoundException.class)
    public void saveThrowsNotFoundExceptionWhenClaimDoesNotExist() {

        when(claimService.getClaimById(eq(CLAIM_ID))).thenThrow(new NotFoundException("not found"));

        defaultJudgmentService.save(USER_ID, "{}", CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasSubmittedBySomeoneElse() {

        final long differentUser = -865564L;

        Claim claim = SampleClaim.getDefault();

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(claim);

        defaultJudgmentService.save(differentUser, "{}", CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasResponded() {

        Claim respondedClaim = SampleClaim.getRespondedAt(LocalDateTime.now().minusDays(2));

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(respondedClaim);

        defaultJudgmentService.save(USER_ID, "{}", CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenUserCannotRequestDefaultJudgmentYet() {

        Claim respondedClaim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(12));

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(respondedClaim);

        defaultJudgmentService.save(USER_ID, "{}", CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenDefaultJudgmentWasAlredySubmitted() {

        Claim respondedClaim = SampleClaim.getDefault();

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(respondedClaim);
        when(defaultJudgmentRepository.getByClaimId(eq(CLAIM_ID))).thenReturn(Optional.of(getDefaultJudgmentModel()));

        defaultJudgmentService.save(USER_ID, "{}", CLAIM_ID);
    }

    private DefaultJudgment getDefaultJudgmentModel() {
        return new DefaultJudgment(DEFAULT_JUDGMENT_ID, CLAIM_ID, USER_ID, EXTERNAL_ID, "{}", LocalDateTime.now());
    }
}
