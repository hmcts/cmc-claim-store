package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDeadlineService;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.rules.PaidInFullRule;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CCDCaseDataToClaim;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
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
    private static final String DEFENDANT_EMAIL = "defendant@email.com";
    private static final Claim claim = createClaimModel(VALID_APP, LETTER_HOLDER_ID);
    private static final String AUTHORISATION = "Bearer: aaa";

    private static final UserDetails validDefendant
        = SampleUserDetails.builder().withUserId(DEFENDANT_ID).withMail(DEFENDANT_EMAIL).build();

    private static final UserDetails validClaimant
        = SampleUserDetails.builder().withUserId(USER_ID).withMail(SUBMITTER_EMAIL).build();

    private static final UserDetails claimantDetails
        = SampleUserDetails.builder().withUserId("11").withMail(SUBMITTER_EMAIL).build();

    private ClaimService claimService;

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private CaseRepository caseRepository;

    @Mock
    private UserService userService;
    @Mock
    private IssueDateCalculator issueDateCalculator;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;
    @Mock
    private EventProducer eventProducer;
    @Mock
    private AppInsights appInsights;
    @Mock
    private CCDCaseDataToClaim ccdCaseDataToClaim;

    @Before
    public void setup() {
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(validDefendant);

        claimService = new ClaimService(
            claimRepository,
            caseRepository,
            userService,
            issueDateCalculator,
            responseDeadlineCalculator,
            directionsQuestionnaireDeadlineCalculator,
            new MoreTimeRequestRule(new ClaimDeadlineService()),
            eventProducer,
            appInsights,
            ccdCaseDataToClaim,
            new PaidInFullRule());
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

        when(claimRepository.getById(eq(CLAIM_ID))).thenReturn(empty());

        claimService.getClaimById(CLAIM_ID);
    }

    @Test
    public void getClaimByLetterHolderIdShouldCallRepositoryWhenValidClaimIsReturned() {

        Claim claim = createClaimModel(VALID_APP, LETTER_HOLDER_ID);
        Optional<Claim> result = Optional.of(claim);

        when(caseRepository.getByLetterHolderId(eq(LETTER_HOLDER_ID), any())).thenReturn(result);

        Claim claimApplication = claimService.getClaimByLetterHolderId(LETTER_HOLDER_ID, AUTHORISATION);
        assertThat(claimApplication).isEqualTo(claim);
    }

    @Test(expected = NotFoundException.class)
    public void getClaimByLetterHolderIdShouldThrowExceptionWhenClaimDoesNotExist() {

        Optional<Claim> result = empty();
        String letterHolderId = "0";

        when(caseRepository.getByLetterHolderId(eq(letterHolderId), any())).thenReturn(result);

        claimService.getClaimByLetterHolderId(letterHolderId, AUTHORISATION);
    }

    @Test(expected = NotFoundException.class)
    public void getClaimByExternalIdShouldThrowExceptionWhenClaimDoesNotExist() {

        Optional<Claim> result = empty();
        String externalId = "does not exist";

        when(caseRepository.getClaimByExternalId(eq(externalId), eq(AUTHORISATION))).thenReturn(result);

        claimService.getClaimByExternalId(externalId, AUTHORISATION);
    }

    @Test
    public void saveClaimShouldFinishSuccessfully() {

        ClaimData claimData = SampleClaimData.validDefaults();

        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(claimantDetails);
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(ISSUE_DATE);
        when(responseDeadlineCalculator.calculateResponseDeadline(eq(ISSUE_DATE))).thenReturn(RESPONSE_DEADLINE);
        when(caseRepository.getOnHoldIdByExternalId(anyString(), eq(AUTHORISATION)))
            .thenReturn(Long.valueOf(1));
        when(caseRepository.getClaimByExternalId(anyString(), eq(AUTHORISATION)))
            .thenReturn(Optional.of(claim));
        when(caseRepository.saveClaim(eq(AUTHORISATION), any())).thenReturn(claim);

        Claim createdClaim = claimService.saveClaim(USER_ID, claimData, AUTHORISATION, singletonList("admissions"));

        assertThat(createdClaim.getClaimData()).isEqualTo(claim.getClaimData());

        verify(eventProducer, once()).createClaimIssuedEvent(eq(createdClaim), eq(null),
            anyString(), eq(AUTHORISATION));
    }

    @Test
    public void requestMoreTimeToRespondShouldFinishSuccessfully() {

        LocalDate newDeadline = RESPONSE_DEADLINE.plusDays(20);

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), anyString()))
            .thenReturn(Optional.of(claim));
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(any()))
            .thenReturn(newDeadline);

        claimService.requestMoreTimeForResponse(EXTERNAL_ID, AUTHORISATION);

        verify(caseRepository, once()).requestMoreTimeForResponse(eq(AUTHORISATION), eq(claim), eq(newDeadline));
        verify(eventProducer, once())
            .createMoreTimeForResponseRequestedEvent(eq(claim), eq(newDeadline), eq(validDefendant.getEmail()));
    }

    @Test(expected = NotFoundException.class)
    public void requestMoreTimeToRespondShouldThrowNotFoundExceptionWhenClaimNotFound() {
        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), anyString())).thenReturn(empty());

        claimService.requestMoreTimeForResponse(EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = MoreTimeRequestedAfterDeadlineException.class)
    public void requestMoreTimeForResponseThrowsMoreTimeRequestedAfterDeadlineWhenItsTooLateForMoreTimeRequest() {

        LocalDate responseDeadlineInThePast = now()
            .minusDays(10);
        Claim claim = createClaimModel(responseDeadlineInThePast, false);

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), anyString()))
            .thenReturn(Optional.of(claim));

        claimService.requestMoreTimeForResponse(EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = MoreTimeAlreadyRequestedException.class)
    public void requestMoreTimeForResponseThrowsMoreTimeAlreadyRequestedExceptionWhenMoreTimeRequestForSecondTime() {
        Claim claim = createClaimModel(RESPONSE_DEADLINE, true);

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), anyString()))
            .thenReturn(Optional.of(claim));

        claimService.requestMoreTimeForResponse(EXTERNAL_ID, AUTHORISATION);
    }

    @Test
    public void getClaimByClaimantEmailShouldCallCaseRepository() {
        when(caseRepository.getByClaimantEmail(eq(claim.getSubmitterEmail()), anyString()))
            .thenReturn(singletonList(claim));

        List<Claim> claims = claimService.getClaimByClaimantEmail(claim.getSubmitterEmail(), AUTHORISATION);

        assertThat(claims).containsExactly(claim);
    }

    @Test
    public void getClaimByDefendantEmailShouldCallCaseRepository() {
        when(caseRepository.getByDefendantEmail(eq(claim.getDefendantEmail()), anyString()))
            .thenReturn(singletonList(claim));

        List<Claim> claims = claimService.getClaimByDefendantEmail(claim.getDefendantEmail(), AUTHORISATION);

        assertThat(claims).containsExactly(claim);
    }

    @Test
    public void saveDefendantResponseShouldUpdateDQDeadlineWhenFullDefenceAndNoMediation() {
        claimService.saveDefendantResponse(
            claim, DEFENDANT_EMAIL, SampleResponse.FullDefence.builder().withMediation(NO).build(), AUTHORISATION
        );

        verify(directionsQuestionnaireDeadlineCalculator)
            .calculateDirectionsQuestionnaireDeadlineCalculator(any());
        verify(caseRepository)
            .updateDirectionsQuestionnaireDeadline(eq(claim), any(), eq(AUTHORISATION));
    }

    @Test
    public void saveDefendantResponseShouldUpdateDQDeadlineWhenFullDefenceAndMediation() {
        claimService.saveDefendantResponse(
            claim, DEFENDANT_EMAIL, SampleResponse.FullDefence.builder().withMediation(YES).build(), AUTHORISATION
        );

        verify(directionsQuestionnaireDeadlineCalculator, never())
            .calculateDirectionsQuestionnaireDeadlineCalculator(any());
        verify(caseRepository, never())
            .updateDirectionsQuestionnaireDeadline(eq(claim), any(), eq(AUTHORISATION));
    }

    @Test
    public void paidInFullShouldFinishSuccessfully() {
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(validClaimant);
        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), anyString()))
            .thenReturn(Optional.of(claim));
        PaidInFull paidInFull = new PaidInFull(now());

        claimService.paidInFull(EXTERNAL_ID, paidInFull, AUTHORISATION);

        verify(caseRepository, once()).paidInFull(eq(claim), eq(paidInFull), eq(AUTHORISATION));

        verify(eventProducer, once()).createPaidInFullEvent(eq(claim));
    }

    @Test(expected = ConflictException.class)
    public void paidInFullShouldThrowConflictExceptionIfAlreadyPaidInFull() {
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(validClaimant);

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), anyString()))
            .thenReturn(Optional.of(createPaidInFullClaim(now())));

        claimService.paidInFull(EXTERNAL_ID, new PaidInFull(now()), AUTHORISATION);
    }

    @Test(expected = NotFoundException.class)
    public void paidInFullShouldThrowNotFoundExceptionWhenClaimNotFound() {
        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), anyString())).thenReturn(empty());

        claimService.paidInFull(EXTERNAL_ID, new PaidInFull(now()), AUTHORISATION);
    }

    private static Claim createClaimModel(ClaimData claimData, String letterHolderId) {
        return createSampleClaim()
            .withClaimData(claimData)
            .withLetterHolderId(letterHolderId)
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
            .withSubmitterEmail(SUBMITTER_EMAIL)
            .withResponseDeadline(responseDeadline)
            .withMoreTimeRequested(moreTimeAlreadyRequested)
            .build();
    }

    private static Claim createPaidInFullClaim(LocalDate moneyReceivedOn) {
        return createSampleClaim()
            .withClaimData(VALID_APP)
            .withMoneyReceivedOn(moneyReceivedOn)
            .build();
    }

    private static SampleClaim createSampleClaim() {
        return SampleClaim.builder()
            .withClaimId(CLAIM_ID)
            .withSubmitterId(USER_ID)
            .withDefendantId(DEFENDANT_ID)
            .withExternalId(EXTERNAL_ID)
            .withReferenceNumber(REFERENCE_NUMBER)
            .withCreatedAt(NOW_IN_LOCAL_ZONE)
            .withIssuedOn(ISSUE_DATE)
            .withResponseDeadline(RESPONSE_DEADLINE)
            .withMoreTimeRequested(NOT_REQUESTED_FOR_MORE_TIME)
            .withSubmitterEmail(SUBMITTER_EMAIL);
    }
}
