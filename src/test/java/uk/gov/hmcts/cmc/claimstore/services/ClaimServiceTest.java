package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.LETTER_HOLDER_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.NOT_REQUESTED_FOR_MORE_TIME;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.SUBMITTER_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.RESPONSE_DEADLINE;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class ClaimServiceTest {

    private static final ClaimData VALID_APP = SampleClaimData.submittedByClaimant();
    private static final String VALID_DEFENDANT_TOKEN = "this is valid token for defendant";
    private static final String DEFENDANT_EMAIL = "defendant@email.com";
    private static final String INVALID_DEFENDANT_TOKEN = "You shall not pass!";
    private static final Claim claim = createClaimModel(VALID_APP, LETTER_HOLDER_ID);
    private static final UserDetails validDefendant = new UserDetails(DEFENDANT_ID, DEFENDANT_EMAIL);
    private static final UserDetails invalidDefendant = new UserDetails(-1L, DEFENDANT_EMAIL);
    private static final UserDetails claimantDetails = new UserDetails(11L, SUBMITTER_EMAIL);

    private ClaimService claimService;

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private JsonMapper mapper;
    @Mock
    private UserService userService;
    @Mock
    private IssueDateCalculator issueDateCalculator;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private EventProducer eventProducer;

    @Before
    public void setup() {
        when(userService.getUserDetails(eq(VALID_DEFENDANT_TOKEN))).thenReturn(validDefendant);

        claimService = new ClaimService(
            claimRepository,
            userService,
            mapper,
            issueDateCalculator,
            responseDeadlineCalculator,
            eventProducer
        );
    }

    @Test
    public void getClaimByIdShouldCallRepositoryWhenValidClaimIsReturned() {

        Optional<Claim> result = Optional.of(claim);

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(result);

        Claim actual = claimService.getClaimById(CLAIM_ID);
        assertThat(actual).isEqualTo(claim);
    }

    @Test
    public void getClaimByLetterHolderIdShouldCallRepositoryWhenValidClaimIsReturned() {

        Long letterHolderId = 1L;
        Claim claim = createClaimModel(VALID_APP, letterHolderId);
        Optional<Claim> result = Optional.of(claim);

        when(claimRepository.getByLetterHolderId(eq(letterHolderId))).thenReturn(result);

        Claim claimApplication = claimService.getClaimByLetterHolderId(letterHolderId);
        assertThat(claimApplication).isEqualTo(claim);
    }

    @Test(expected = NotFoundException.class)
    public void getClaimByLetterHolderIdShouldThrowExceptionWhenClaimDoesNotExist() {

        Optional<Claim> result = Optional.empty();
        Long letterHolderId = 0L;

        when(claimRepository.getByLetterHolderId(eq(letterHolderId))).thenReturn(result);

        claimService.getClaimByLetterHolderId(letterHolderId);
    }

    @Test(expected = NotFoundException.class)
    public void getClaimByExternalIdShouldThrowExceptionWhenClaimDoesNotExist() {

        Optional<Claim> result = Optional.empty();
        String externalId = "does not exist";

        when(claimRepository.getClaimByExternalId(eq(externalId))).thenReturn(result);

        claimService.getClaimByExternalId(externalId);
    }

    @Test
    public void saveClaimShouldFinishSuccessfully() {

        ClaimData app = SampleClaimData.validDefaults();
        String jsonApp = new ResourceReader().read("/claim-application.json");
        final String authorisationToken = "Open sesame!";

        when(userService.getUserDetails(eq(authorisationToken))).thenReturn(claimantDetails);
        when(mapper.toJson(eq(app))).thenReturn(jsonApp);
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(ISSUE_DATE);
        when(responseDeadlineCalculator.calculateResponseDeadline(eq(ISSUE_DATE))).thenReturn(RESPONSE_DEADLINE);

        when(claimRepository.saveRepresented(
            eq(jsonApp),
            eq(USER_ID),
            eq(ISSUE_DATE),
            eq(RESPONSE_DEADLINE),
            anyString(),
            eq(SUBMITTER_EMAIL)
        )).thenReturn(CLAIM_ID);

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(claim));

        Claim createdClaim = claimService.saveClaim(USER_ID, app, authorisationToken);

        assertThat(createdClaim).isEqualTo(claim);
        verify(eventProducer, once()).createClaimIssuedEvent(eq(createdClaim), eq(null));
    }

    @Test
    public void requestMoreTimeToRespondShouldFinishSuccessfully() {

        LocalDate newDeadline = RESPONSE_DEADLINE.plusDays(20);

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(claim));
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(eq(RESPONSE_DEADLINE)))
            .thenReturn(newDeadline);

        claimService.requestMoreTimeForResponse(CLAIM_ID, VALID_DEFENDANT_TOKEN);

        verify(claimRepository, once()).requestMoreTime(eq(CLAIM_ID), eq(newDeadline));
        verify(eventProducer, once())
            .createMoreTimeForResponseRequestedEvent(eq(claim), eq(newDeadline), eq(validDefendant.getEmail()));
    }

    @Test(expected = ForbiddenActionException.class)
    public void requestMoreTimeToRespondShouldThrowForbiddenActionExceptionWhenClaimIsNotLinkedToDefendant() {
        when(userService.getUserDetails(eq(INVALID_DEFENDANT_TOKEN))).thenReturn(invalidDefendant);
        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(claim));

        claimService.requestMoreTimeForResponse(CLAIM_ID, INVALID_DEFENDANT_TOKEN);
    }

    @Test(expected = NotFoundException.class)
    public void requestMoreTimeToRespondShouldThrowNotFoundExceptionWhenClaimNotFound() {

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.empty());

        claimService.requestMoreTimeForResponse(CLAIM_ID, VALID_DEFENDANT_TOKEN);
    }

    @Test(expected = MoreTimeRequestedAfterDeadlineException.class)
    public void requestMoreTimeForResponseThrowsMoreTimeRequestedAfterDeadlineWhenItsTooLateForMoreTimeRequest() {

        LocalDate responseDeadlineInThePast = LocalDate.now()
            .minusDays(10);
        Claim claim = createClaimModel(responseDeadlineInThePast, false);

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(claim));

        claimService.requestMoreTimeForResponse(CLAIM_ID, VALID_DEFENDANT_TOKEN);
    }

    @Test(expected = MoreTimeAlreadyRequestedException.class)
    public void requestMoreTimeForResponseThrowsMoreTimeAlreadyRequestedExceptionWhenMoreTimeRequestForSecondTime() {
        Claim claim = createClaimModel(RESPONSE_DEADLINE, true);

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(claim));

        claimService.requestMoreTimeForResponse(CLAIM_ID, VALID_DEFENDANT_TOKEN);
    }

    private static Claim createClaimModel(ClaimData claimData, Long letterHolderId) {
        return new Claim(
            CLAIM_ID,
            USER_ID,
            letterHolderId,
            DEFENDANT_ID,
            EXTERNAL_ID,
            REFERENCE_NUMBER,
            claimData,
            NOW_IN_LOCAL_ZONE,
            ISSUE_DATE,
            RESPONSE_DEADLINE,
            NOT_REQUESTED_FOR_MORE_TIME,
            SUBMITTER_EMAIL,
            null, null, null, null, null);
    }

    private static Claim createClaimModel(LocalDate responseDeadline, boolean moreTimeAlreadyRequested) {
        return new Claim(
            CLAIM_ID,
            USER_ID,
            LETTER_HOLDER_ID,
            DEFENDANT_ID,
            EXTERNAL_ID,
            REFERENCE_NUMBER,
            VALID_APP,
            NOW_IN_LOCAL_ZONE,
            ISSUE_DATE,
            responseDeadline,
            moreTimeAlreadyRequested,
            SUBMITTER_EMAIL,
            null, null, null, null, null);
    }
}
