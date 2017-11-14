package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.EmailContentTemplates;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.ContentProvider;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class CountyCourtJudgmentServiceTest {

    private static final CountyCourtJudgment DATA = SampleCountyCourtJudgment.builder().build();

    private CountyCourtJudgmentService countyCourtJudgmentService;

    @Mock
    private ClaimService claimService;
    @Mock
    private JsonMapper jsonMapper;
    @Mock
    private EventProducer eventProducer;

    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private InterestCalculationService interestCalculationService;

    private EmailContentTemplates emailTemplates = new EmailContentTemplates();

    private ContentProvider contentProvider;

    @Before
    public void setup() {
        contentProvider = new ContentProvider(interestCalculationService);

        countyCourtJudgmentService = new CountyCourtJudgmentService(
            claimService,
            eventProducer,
            pdfServiceClient,
            emailTemplates,
            contentProvider
        );
    }

    @Test
    public void saveShouldFinishSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2)).build();

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(claim);

        countyCourtJudgmentService.save(USER_ID, DATA, CLAIM_ID);

        verify(eventProducer, once()).createCountyCourtJudgmentRequestedEvent(any(Claim.class));
        verify(claimService, once()).saveCountyCourtJudgment(eq(CLAIM_ID), any());
    }

    @Test(expected = NotFoundException.class)
    public void saveThrowsNotFoundExceptionWhenClaimDoesNotExist() {

        when(claimService.getClaimById(eq(CLAIM_ID))).thenThrow(new NotFoundException("Claim not found by id"));

        countyCourtJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasSubmittedBySomeoneElse() {

        final String differentUser = "34234234";

        Claim claim = SampleClaim.getDefault();

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(claim);

        countyCourtJudgmentService.save(differentUser, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasResponded() {

        Claim respondedClaim = SampleClaim.builder().withRespondedAt(LocalDateTime.now().minusDays(2)).build();

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(respondedClaim);

        countyCourtJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenUserCannotRequestCountyCourtJudgmentYet() {

        Claim respondedClaim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(12));

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(respondedClaim);

        countyCourtJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenCountyCourtJudgmentWasAlreadySubmitted() {

        Claim respondedClaim = SampleClaim.getDefault();

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(respondedClaim);

        countyCourtJudgmentService.save(USER_ID, DATA, CLAIM_ID);
    }
}
