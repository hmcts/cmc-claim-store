package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.CountyCourtJudgmentAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ResponseAlreadySubmittedException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseServiceTest {

    private static final Response VALID_APP = SampleResponse.validDefaults();
    private static final Claim claim = SampleClaim.getDefault();
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String DEFENDANT_EMAIL = "test@example.com";

    private DefendantResponseService responseService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private UserService userService;

    @Mock
    private ClaimService claimService;

    @Before
    public void setup() {
        responseService = new DefendantResponseService(
            eventProducer,
            claimService,
            userService,
            new AuthorisationService()
        );
    }

    @Test
    public void saveShouldFinishSuccessfully() {
        //given
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(
            new UserDetails(USER_ID, DEFENDANT_EMAIL, "Jonny", "Jones")
        );
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(
            SampleUserDetails.builder().withUserId(USER_ID).withMail(DEFENDANT_EMAIL).build());

        when(claimService.getClaimById(eq(CLAIM_ID))).thenReturn(claim);

        //when
        responseService.save(CLAIM_ID, DEFENDANT_ID, VALID_APP, AUTHORISATION);

        //then
        verify(eventProducer, once())
            .createDefendantResponseEvent(eq(claim));
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveShouldThrowForbiddenActionWhenUserIsNotDefendant() {

        when(claimService.getClaimById(eq(CLAIM_ID)))
            .thenReturn(SampleClaim.getDefault());

        responseService.save(CLAIM_ID, "23132", VALID_APP, AUTHORISATION);
    }


    @Test(expected = ResponseAlreadySubmittedException.class)
    public void saveShouldThrowResponseAlreadySubmittedExceptionWhenResponseSubmitted() {

        when(claimService.getClaimById(eq(CLAIM_ID)))
            .thenReturn(SampleClaim.builder().withRespondedAt(LocalDateTime.now()).build());

        responseService.save(CLAIM_ID, DEFENDANT_ID, VALID_APP, AUTHORISATION);
    }

    @Test(expected = CountyCourtJudgmentAlreadyRequestedException.class)
    public void saveShouldThrowCountyCourtJudgmentAlreadyRequestedExceptionWhenCCJRequested() {

        when(claimService.getClaimById(eq(CLAIM_ID)))
            .thenReturn(SampleClaim.builder().withCountyCourtJudgmentRequestedAt(LocalDateTime.now()).build());

        responseService.save(CLAIM_ID, DEFENDANT_ID, VALID_APP, AUTHORISATION);
    }
}
