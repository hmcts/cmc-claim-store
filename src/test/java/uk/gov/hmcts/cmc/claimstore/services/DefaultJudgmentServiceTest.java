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
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJudgmentServiceTest {

    private static final long DEFAULT_JUDGMENT_ID = 33L;
    private static final Map<String, Object> DATA = new HashMap<>();

    private DefaultJudgmentService defaultJudgmentService;

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private JsonMapper jsonMapper;
    @Mock
    private EventProducer eventProducer;

    @Before
    public void setup() {
        defaultJudgmentService = new DefaultJudgmentService(
            claimRepository,
            jsonMapper,
            eventProducer
        );
    }

    @Test
    public void saveShouldFinishSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.getWithResponseDeadline(LocalDate.now().minusMonths(2));

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(claim));

        defaultJudgmentService.save(USER_ID, DATA, CLAIM_ID);

        verify(eventProducer, once()).createDefaultJudgmentSubmittedEvent(any(Claim.class));
        verify(claimRepository, once()).saveDefaultJudgment(eq(CLAIM_ID), any());
    }

    @Test(expected = NotFoundException.class)
    public void saveThrowsNotFoundExceptionWhenClaimDoesNotExist() {

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.empty());

        defaultJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasSubmittedBySomeoneElse() {

        final long differentUser = -865564L;

        Claim claim = SampleClaim.getDefault();

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(claim));

        defaultJudgmentService.save(differentUser, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasResponded() {

        Claim respondedClaim = SampleClaim.builder().withRespondedAt(LocalDateTime.now().minusDays(2)).build();

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(respondedClaim));

        defaultJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenUserCannotRequestDefaultJudgmentYet() {

        Claim respondedClaim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(12));

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(respondedClaim));

        defaultJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenDefaultJudgmentWasAlreadySubmitted() {

        Claim respondedClaim = SampleClaim.getDefault();

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(respondedClaim));

        defaultJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }
}
