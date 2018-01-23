package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.services.search.DBCaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.LETTER_HOLDER_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.NOT_REQUESTED_FOR_MORE_TIME;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.SUBMITTER_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.RESPONSE_DEADLINE;

@RunWith(MockitoJUnitRunner.class)
public class ClaimServiceTest {

    private static final ClaimData VALID_APP = SampleClaimData.submittedByClaimant();
    private static final String VALID_DEFENDANT_TOKEN = "this is valid token for defendant";
    private static final String DEFENDANT_EMAIL = "defendant@email.com";
    private static final String INVALID_DEFENDANT_TOKEN = "You shall not pass!";
    private static final Claim claim = createClaimModel(VALID_APP, LETTER_HOLDER_ID);
    private static final String AUTHORISATION = "Bearer: aaa";

    private static final UserDetails validDefendant
        = SampleUserDetails.builder().withUserId(DEFENDANT_ID).withMail(DEFENDANT_EMAIL).build();

    private static final UserDetails invalidDefendant
        = SampleUserDetails.builder().withUserId("-1").withMail(DEFENDANT_EMAIL).build();

    private static final UserDetails claimantDetails
        = SampleUserDetails.builder().withUserId("11").withMail(SUBMITTER_EMAIL).build();

    private ClaimService claimService;

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private DBCaseRepository caseRepository;
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
            eventProducer,
            caseRepository);
    }

    @Test
    public void getClaimByIdShouldCallRepositoryWhenValidClaimIsReturned() {

        Optional<Claim> result = Optional.of(claim);

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(result);

        Claim actual = claimService.getClaimById(CLAIM_ID);
        assertThat(actual).isEqualTo(claim);
    }

    @Test(expected = NotFoundException.class)
    public void getClaimByIdShouldThrowNotFoundException() {

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.empty());

        claimService.getClaimById(CLAIM_ID);
    }

    @Test
    public void getClaimByLetterHolderIdShouldCallRepositoryWhenValidClaimIsReturned() {

        Claim claim = createClaimModel(VALID_APP, LETTER_HOLDER_ID);
        Optional<Claim> result = Optional.of(claim);

        when(claimRepository.getByLetterHolderId(eq(LETTER_HOLDER_ID))).thenReturn(result);

        Claim claimApplication = claimService.getClaimByLetterHolderId(LETTER_HOLDER_ID);
        assertThat(claimApplication).isEqualTo(claim);
    }

    @Test(expected = NotFoundException.class)
    public void getClaimByLetterHolderIdShouldThrowExceptionWhenClaimDoesNotExist() {

        Optional<Claim> result = Optional.empty();
        String letterHolderId = "0";

        when(claimRepository.getByLetterHolderId(eq(letterHolderId))).thenReturn(result);

        claimService.getClaimByLetterHolderId(letterHolderId);
    }

    @Test(expected = NotFoundException.class)
    public void getClaimByExternalIdShouldThrowExceptionWhenClaimDoesNotExist() {

        Optional<Claim> result = Optional.empty();
        String externalId = "does not exist";

        when(caseRepository.getClaimByExternalId(eq(externalId), eq(AUTHORISATION))).thenReturn(result);

        claimService.getClaimByExternalId(externalId, AUTHORISATION);
    }

    @Test
    public void saveClaimShouldFinishSuccessfully() {

        ClaimData app = SampleClaimData.validDefaults();
        String jsonApp = new ResourceReader().read("/claim-application.json");
        String authorisationToken = "Open sesame!";

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

        verify(eventProducer, once()).createClaimIssuedEvent(eq(createdClaim), eq(null),
            anyString(), eq(authorisationToken));
    }

    @Test(expected = ConflictException.class)
    public void saveClaimShouldThrowConflictExceptionForDuplicateClaim() {
        ClaimData app = SampleClaimData.validDefaults();
        String authorisationToken = "Open same!";
        when(caseRepository.getClaimByExternalId(any(), anyString())).thenReturn(Optional.of(claim));

        claimService.saveClaim(USER_ID, app, authorisationToken);
    }

    @Test
    public void requestMoreTimeToRespondShouldFinishSuccessfully() {

        LocalDate newDeadline = RESPONSE_DEADLINE.plusDays(20);

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(Optional.of(claim));
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(eq(ISSUE_DATE)))
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

    private static Claim createClaimModel(ClaimData claimData, String letterHolderId) {
        return SampleClaim.builder()
            .withClaimId(CLAIM_ID)
            .withSubmitterId(USER_ID)
            .withLetterHolderId(letterHolderId)
            .withDefendantId(DEFENDANT_ID)
            .withExternalId(EXTERNAL_ID)
            .withReferenceNumber(REFERENCE_NUMBER)
            .withClaimData(claimData)
            .withCreatedAt(NOW_IN_LOCAL_ZONE)
            .withIssuedOn(ISSUE_DATE)
            .withResponseDeadline(RESPONSE_DEADLINE)
            .withMoreTimeRequested(NOT_REQUESTED_FOR_MORE_TIME)
            .withSubmitterEmail(SUBMITTER_EMAIL)
            .build();
    }

    private static Claim createClaimModel(LocalDate responseDeadline, boolean moreTimeAlreadyRequested) {
        return SampleClaim.builder()
            .withClaimId(CLAIM_ID)
            .withSubmitterId(USER_ID)
            .withLetterHolderId(LETTER_HOLDER_ID)
            .withDefendantId(DEFENDANT_ID)
            .withExternalId(EXTERNAL_ID)
            .withReferenceNumber(REFERENCE_NUMBER)
            .withClaimData(VALID_APP)
            .withCreatedAt(NOW_IN_LOCAL_ZONE)
            .withIssuedOn(ISSUE_DATE)
            .withResponseDeadline(responseDeadline)
            .withMoreTimeRequested(moreTimeAlreadyRequested)
            .withSubmitterEmail(SUBMITTER_EMAIL)
            .build();
    }
}
