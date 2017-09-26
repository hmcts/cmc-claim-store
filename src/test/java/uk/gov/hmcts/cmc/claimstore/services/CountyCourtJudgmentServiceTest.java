package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.CCJContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class CountyCourtJudgmentServiceTest {

    private static final CountyCourtJudgment DATA = SampleCountyCourtJudgment.builder().build();

    private CountyCourtJudgmentService countyCourtJudgmentService;

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private JsonMapper jsonMapper;
    @Mock
    private EventProducer eventProducer;

    @Mock
    private InterestCalculationService interestCalculationService;

    @Mock
    private PDFServiceClient pdfServiceClient;

    private StaffEmailTemplates emailTemplates = new StaffEmailTemplates();

    private CCJContentProvider ccjContentProvider;

    @Before
    public void setup() {
        ccjContentProvider = new CCJContentProvider(new InterestContentProvider(interestCalculationService));

        countyCourtJudgmentService = new CountyCourtJudgmentService(
            claimRepository,
            jsonMapper,
            eventProducer,
            pdfServiceClient,
            emailTemplates,
            ccjContentProvider
        );
    }

    @Test
    public void saveShouldFinishSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.getWithResponseDeadline(LocalDate.now().minusMonths(2));

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(claim));

        countyCourtJudgmentService.save(USER_ID, DATA, CLAIM_ID);

        verify(eventProducer, once()).createCountyCourtJudgmentSubmittedEvent(any(Claim.class));
        verify(claimRepository, once()).saveCountyCourtJudgment(eq(CLAIM_ID), any());
    }

    @Test(expected = NotFoundException.class)
    public void saveThrowsNotFoundExceptionWhenClaimDoesNotExist() {

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.empty());

        countyCourtJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasSubmittedBySomeoneElse() {

        final long differentUser = -865564L;

        Claim claim = SampleClaim.getDefault();

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(claim));

        countyCourtJudgmentService.save(differentUser, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasResponded() {

        Claim respondedClaim = SampleClaim.builder().withRespondedAt(LocalDateTime.now().minusDays(2)).build();

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(respondedClaim));

        countyCourtJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenUserCannotRequestCountyCourtJudgmentYet() {

        Claim respondedClaim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(12));

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(respondedClaim));

        countyCourtJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenCountyCourtJudgmentWasAlreadySubmitted() {

        Claim respondedClaim = SampleClaim.getDefault();

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(respondedClaim));

        countyCourtJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }
}
