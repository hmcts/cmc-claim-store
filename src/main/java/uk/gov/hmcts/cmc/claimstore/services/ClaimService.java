package uk.gov.hmcts.cmc.claimstore.services;

import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.filters.DocumentsFilter;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimAuthorisationRule;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.rules.PaidInFullRule;
import uk.gov.hmcts.cmc.claimstore.rules.ReviewOrderRule;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.domain.models.ioc.CreatePaymentResponse;
import uk.gov.hmcts.cmc.domain.models.legalrep.LegalRepUpdate;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESET_CLAIM_SUBMISSION_OPERATION_INDICATORS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESUME_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.UPDATE_HELP_WITH_FEE_CLAIM;
import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.CLAIM_EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BREATHING_SPACE_LIFTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_LEGAL;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_LEGAL_CREATE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.HWF_CLAIM_CREATED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NUMBER_OF_RECONSIDERATION;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_PAYMENT;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_RESPONSE_HWF;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.BUSINESS_QUEUE;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.CLOSED_HWF;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.HWF_APPLICATION_PENDING;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.PROCEEDS_IN_CASE_MAN;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.SETTLED;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.TRANSFERRED;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Component
public class ClaimService {

    private final IssueDateCalculator issueDateCalculator;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final UserService userService;
    private final EventProducer eventProducer;
    private final CaseRepository caseRepository;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final AppInsights appInsights;
    private final PaidInFullRule paidInFullRule;
    private final ClaimAuthorisationRule claimAuthorisationRule;
    private final ReviewOrderRule reviewOrderRule;
    private final LaunchDarklyClient launchDarklyClient;

    @Value("${feature_toggles.ctsc_enabled}")
    private boolean ctscEnabled;

    @SuppressWarnings("squid:S00107")
    @Autowired
    public ClaimService(
        CaseRepository caseRepository,
        UserService userService,
        IssueDateCalculator issueDateCalculator,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        EventProducer eventProducer,
        AppInsights appInsights,
        PaidInFullRule paidInFullRule,
        ClaimAuthorisationRule claimAuthorisationRule,
        ReviewOrderRule reviewOrderRule,
        LaunchDarklyClient launchDarklyClient) {
        this.userService = userService;
        this.issueDateCalculator = issueDateCalculator;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.eventProducer = eventProducer;
        this.caseRepository = caseRepository;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.appInsights = appInsights;
        this.paidInFullRule = paidInFullRule;
        this.claimAuthorisationRule = claimAuthorisationRule;
        this.reviewOrderRule = reviewOrderRule;
        this.launchDarklyClient = launchDarklyClient;
    }

    public List<Claim> getClaimBySubmitterId(String submitterId, String authorisation, Integer pageNumber) {
        claimAuthorisationRule.assertUserIdMatchesAuthorisation(submitterId, authorisation);
        return caseRepository.getBySubmitterId(submitterId, authorisation, pageNumber);
    }

    public Claim getClaimByLetterHolderId(String id, String authorisation) {
        Claim claim = caseRepository
            .getByLetterHolderId(id, authorisation)
            .orElseThrow(() -> new NotFoundException("Claim not found for letter holder id " + id));

        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);

        return claim;
    }

    public Claim getFilteredClaimByExternalId(String externalId, String authorisation) {
        User user = userService.getUser(authorisation);
        Claim claim = getClaimByExternalId(externalId, user);

        return DocumentsFilter.filterDocuments(
            claim, user.getUserDetails(), ctscEnabled,
            launchDarklyClient.isFeatureEnabled("paper-response-review-new-handling")
        );
    }

    public Claim getClaimByExternalId(String externalId, String authorisation) {
        User user = userService.getUser(authorisation);
        return getClaimByExternalId(externalId, user);
    }

    public Claim getClaimByExternalId(String externalId, User user) {
        Claim claim = caseRepository
            .getClaimByExternalId(externalId, user)
            .orElseThrow(() -> new NotFoundException("Claim not found by external id " + externalId));

        claimAuthorisationRule.assertClaimCanBeAccessed(claim, user);

        return claim;
    }

    public Optional<Claim> getClaimByReference(String reference, String authorisation) {
        Optional<Claim> claim = caseRepository
            .getByClaimReferenceNumber(reference, authorisation);

        claim.ifPresent(c -> claimAuthorisationRule.assertClaimCanBeAccessed(c, authorisation));

        return claim;
    }

    public Optional<Claim> getClaimByReferenceAnonymous(String reference) {
        String authorisation = null;

        if (caseRepository instanceof CCDCaseRepository) {
            User user = userService.authenticateAnonymousCaseWorker();
            authorisation = user.getAuthorisation();
        }

        return caseRepository.getByClaimReferenceNumber(reference, authorisation);
    }

    public List<Claim> getClaimByExternalReference(String externalReference, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();

        return asStream(caseRepository.getBySubmitterId(submitterId, authorisation, null))
            .filter(claim ->
                claim.getClaimData().getExternalReferenceNumber().filter(externalReference::equals).isPresent())
            .collect(Collectors.toList());
    }

    public List<Claim> getClaimByDefendantId(String id, String authorisation, Integer pageNumber) {
        claimAuthorisationRule.assertUserIdMatchesAuthorisation(id, authorisation);
        return caseRepository.getByDefendantId(id, authorisation, pageNumber);
    }

    public Map<String, String> getPaginationInfo(String authorisation, String userType) {
        return caseRepository.getPaginationInfo(authorisation, userType);
    }

    public List<Claim> getClaimByClaimantEmail(String email, String authorisation) {
        return caseRepository.getByClaimantEmail(email, authorisation);
    }

    public List<Claim> getClaimByDefendantEmail(String email, String authorisation) {
        return caseRepository.getByDefendantEmail(email, authorisation);
    }

    public List<Claim> getClaimByPaymentReference(String payReference, String authorisation) {
        return caseRepository.getByPaymentReference(payReference, authorisation);
    }

    public List<Claim> getClaimsByState(ClaimState claimState, User user) {
        return caseRepository.getClaimsByState(claimState, user);
    }

    @LogExecutionTime
    public CreatePaymentResponse initiatePayment(
        String authorisation,
        ClaimData claimData
    ) {
        User user = userService.getUser(authorisation);

        Claim claim = buildClaimFrom(user,
            user.getUserDetails().getId(),
            claimData,
            emptyList(), false);

        Claim createdClaim = caseRepository.initiatePayment(user, claim);

        Payment payment = createdClaim.getClaimData().getPayment()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT));
        return CreatePaymentResponse.builder()
            .nextUrl(payment.getNextUrl())
            .build();
    }

    @LogExecutionTime
    public CreatePaymentResponse resumePayment(String authorisation, ClaimData claimData) {
        User user = userService.getUser(authorisation);
        Claim claim = getClaimByExternalId(claimData.getExternalId().toString(), user)
            .toBuilder()
            .claimData(claimData)
            .build();

        Claim resumedClaim = caseRepository.saveCaseEventIOC(user, claim, RESUME_CLAIM_PAYMENT_CITIZEN);

        Payment payment = resumedClaim.getClaimData().getPayment()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT));
        String returnUrl = resumedClaim.getClaimData().getPayment()
            .orElseThrow(IllegalStateException::new).getReturnUrl();
        return CreatePaymentResponse.builder()
            .nextUrl(
                payment.getStatus().equals(PaymentStatus.SUCCESS)
                    ? returnUrl
                    : payment.getNextUrl()
            )
            .build();
    }

    @LogExecutionTime
    public Claim createCitizenClaim(
        String authorisation,
        ClaimData claimData,
        List<String> features
    ) {
        User user = userService.getUser(authorisation);
        Claim claim = getClaimByExternalId(claimData.getExternalId().toString(), user)
            .toBuilder()
            .features(features)
            .build();

        return caseRepository.saveCaseEventIOC(user, claim, CREATE_CITIZEN_CLAIM);
    }

    @LogExecutionTime
    public Claim saveClaim(
        String submitterId,
        ClaimData claimData,
        String authorisation,
        List<String> features
    ) {
        String externalId = claimData.getExternalId().toString();
        User user = userService.getUser(authorisation);
        caseRepository.getClaimByExternalId(externalId, user)
            .ifPresent(claim -> {
                throw new ConflictException(
                    String.format("Claim already exist with same external reference as %s", externalId));
            });

        Claim claim = buildClaimFrom(user,
            submitterId,
            claimData,
            features, false);

        Claim savedClaim = caseRepository.saveClaim(user, claim);
        createClaimEvent(authorisation, user, savedClaim);
        trackClaimIssued(savedClaim.getReferenceNumber(), savedClaim.getClaimData().isClaimantRepresented());

        return savedClaim;
    }

    @LogExecutionTime
    public Claim saveHelpWithFeesClaim(
        String submitterId,
        ClaimData claimData,
        String authorisation,
        List<String> features
    ) {
        String externalId = claimData.getExternalId().toString();
        User user = userService.getUser(authorisation);
        caseRepository.getClaimByExternalId(externalId, user)
            .ifPresent(claim -> {
                throw new ConflictException("Claim already exist with same external reference as " + externalId);
            });

        Claim claim = buildClaimFrom(user, submitterId, claimData, features, true);
        trackHelpWithFeesClaimCreated(externalId);
        return caseRepository.saveHelpWithFeesClaim(user, claim);
    }

    @LogExecutionTime
    public Claim updateHelpWithFeesClaim(
        String authorisation,
        ClaimData claimData,
        List<String> features
    ) {
        String externalId = claimData.getExternalId().toString();
        User user = userService.getUser(authorisation);
        Claim claim = getClaimByExternalId(claimData.getExternalId().toString(), user)
            .toBuilder()
            .claimData(claimData)
            .features(features)
            .build();

        trackHelpWithFeesClaimCreated(externalId);
        return caseRepository.updateHelpWithFeesClaim(user, claim, UPDATE_HELP_WITH_FEE_CLAIM);
    }

    @LogExecutionTime
    public Claim saveRepresentedClaim(
        String submitterId,
        ClaimData claimData,
        String authorisation
    ) {
        String externalId = claimData.getExternalId().toString();
        User user = userService.getUser(authorisation);

        String submitterEmail = user.getUserDetails().getEmail();

        Claim claim = Claim.builder()
            .claimData(claimData)
            .submitterId(submitterId)
            .externalId(externalId)
            .submitterEmail(submitterEmail)
            .createdAt(LocalDateTimeFactory.nowInUTC())
            .claimSubmissionOperationIndicators(ClaimSubmissionOperationIndicators.builder().build())
            .build();

        Claim savedClaim = caseRepository.saveRepresentedClaim(user, claim);
        AppInsightsEvent event = CLAIM_LEGAL_CREATE;
        appInsights.trackEvent(event, REFERENCE_NUMBER, savedClaim.getReferenceNumber());
        return savedClaim;
    }

    @LogExecutionTime
    public Claim updateRepresentedClaim(
        String submitterId,
        LegalRepUpdate legalRepUpdate,
        String authorisation
    ) {
        var claim = getClaimByExternalId(legalRepUpdate.getExternalId(), authorisation);
        var user = userService.getUser(authorisation);

        var savedClaim = caseRepository.updateRepresentedClaim(submitterId, user, claim, legalRepUpdate);
        if (PaymentStatus.fromValue(legalRepUpdate.getPaymentReference().getStatus()).equals(PaymentStatus.SUCCESS)) {
            createClaimEvent(authorisation, user, savedClaim);
        }
        trackClaimIssued(savedClaim.getReferenceNumber(), savedClaim.getClaimData().isClaimantRepresented());

        return savedClaim;
    }

    private void trackClaimIssued(String referenceNumber, boolean represented) {
        AppInsightsEvent event = represented ? CLAIM_ISSUED_LEGAL : CLAIM_ISSUED_CITIZEN;
        appInsights.trackEvent(event, REFERENCE_NUMBER, referenceNumber);
    }

    private void trackHelpWithFeesClaimCreated(String externalId) {
        appInsights.trackEvent(HWF_CLAIM_CREATED, CLAIM_EXTERNAL_ID, externalId);
    }

    public Claim requestMoreTimeForResponse(String externalId, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        LocalDate issuedOn = null;
        Optional<LocalDate> issuedOnOptional = claim.getIssuedOn();
        if (issuedOnOptional.isPresent()) {
            issuedOn = issuedOnOptional.get();
        }
        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(issuedOn);
        this.moreTimeRequestRule.assertMoreTimeCanBeRequested(claim);
        caseRepository.requestMoreTimeForResponse(authorisation, claim, newDeadline);
        return claim;
    }

    public void linkDefendantToClaim(String authorisation, String letterholderId) {
        caseRepository.linkDefendant(authorisation, letterholderId);
    }

    public Claim saveClaimDocuments(
        String authorisation,
        Long claimId,
        ClaimDocumentCollection claimDocumentCollection,
        ClaimDocumentType claimDocumentType
    ) {
        return caseRepository.saveClaimDocuments(authorisation, claimId, claimDocumentCollection, claimDocumentType);
    }

    public Claim linkLetterHolder(Claim claim, String letterHolderId) {
        return caseRepository.linkLetterHolder(claim.getId(), letterHolderId);
    }

    public void saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment
    ) {
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);
        caseRepository.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);
    }

    public void saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        String authorization
    ) {
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorization);
        LocalDate claimantResponseDeadline =
            responseDeadlineCalculator.calculateClaimantResponseDeadline(LocalDate.now());
        caseRepository.saveDefendantResponse(claim, defendantEmail, response, claimantResponseDeadline, authorization);
    }

    public Claim paidInFull(String externalId, PaidInFull paidInFull, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);
        String claimantId = userService.getUserDetails(authorisation).getId();
        this.paidInFullRule.assertPaidInFull(claim, claimantId);
        this.caseRepository.paidInFull(claim, paidInFull, authorisation);
        Claim updatedClaim = getClaimByExternalId(externalId, authorisation);
        this.eventProducer.createPaidInFullEvent(updatedClaim);
        appInsights.trackEvent(AppInsightsEvent.PAID_IN_FULL, REFERENCE_NUMBER, claim.getReferenceNumber());
        return updatedClaim;
    }

    public void saveReDetermination(
        String authorisation,
        Claim claim,
        ReDetermination redetermination
    ) {
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);
        caseRepository.saveReDetermination(authorisation, claim, redetermination);
    }

    public void updateClaimState(String authorisation, Claim claim, ClaimState currentState) {
        caseRepository.updateClaimState(authorisation, claim.getId(), currentState);
    }

    public Claim updateClaimSubmissionOperationIndicators(
        String authorisation,
        Claim claim,
        ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators
    ) {
        return caseRepository.updateClaimSubmissionOperationStatus(
            authorisation,
            claim.getId(),
            claimSubmissionOperationIndicators,
            RESET_CLAIM_SUBMISSION_OPERATION_INDICATORS
        );
    }

    public Claim saveReviewOrder(String externalId, ReviewOrder reviewOrder, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);
        reviewOrderRule.assertReviewOrder(claim);
        Claim updatedClaim = caseRepository.saveReviewOrder(claim.getId(), reviewOrder, authorisation);
        eventProducer.createReviewOrderEvent(authorisation, updatedClaim);
        appInsights.trackEvent(NUMBER_OF_RECONSIDERATION, REFERENCE_NUMBER, claim.getReferenceNumber());
        return updatedClaim;
    }

    public Claim addBulkPrintDetails(
        String authorisation,
        List<BulkPrintDetails> bulkPrintCollection,
        CaseEvent caseEvent,
        Claim claim
    ) {
        return caseRepository.addBulkPrintDetailsToClaim(
            authorisation,
            bulkPrintCollection,
            caseEvent,
            claim
        );
    }

    private Claim buildClaimFrom(
        User user,
        String submitterId,
        ClaimData claimData,
        List<String> features,
        boolean helpWithFees
    ) {
        String externalId = claimData.getExternalId().toString();

        LocalDate issuedOn = issueDateCalculator.calculateIssueDay(nowInLocalZone());
        String submitterEmail = user.getUserDetails().getEmail();

        Claim.ClaimBuilder claimBuilder = Claim.builder()
            .claimData(claimData)
            .submitterId(submitterId)
            .issuedOn(issuedOn)
            .serviceDate(issuedOn.plusDays(5))
            .externalId(externalId)
            .submitterEmail(submitterEmail)
            .createdAt(LocalDateTimeFactory.nowInUTC())
            .features(features)
            .claimSubmissionOperationIndicators(ClaimSubmissionOperationIndicators.builder().build());

        if (!helpWithFees) {
            claimBuilder
                .issuedOn(issuedOn)
                .serviceDate(issuedOn.plusDays(5))
                .responseDeadline(responseDeadlineCalculator.calculateResponseDeadline(issuedOn));
        }
        return claimBuilder.build();
    }

    private void createClaimEvent(String authorisation, User user, Claim savedClaim) {
        eventProducer.createClaimCreatedEvent(
            savedClaim,
            user.getUserDetails().getFullName(),
            authorisation
        );

    }

    @LogExecutionTime
    public Claim saveBreathingSpaceDetails(
        String externalId,
        BreathingSpace breathingSpace,
        String authorisation
    ) throws HttpException {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);
        String validatedBsDetails = validateBreathingSpaceDetails(breathingSpace, claim);
        if (validatedBsDetails == null) {
            Claim updatedClaim = caseRepository.saveBreathingSpaceDetails(claim, breathingSpace, authorisation);

            if (breathingSpace.getBsLiftedFlag().equals("No")) {
                appInsights.trackEvent(BREATHING_SPACE_ENTERED, CLAIM_EXTERNAL_ID, externalId);
            } else {
                appInsights.trackEvent(BREATHING_SPACE_LIFTED, CLAIM_EXTERNAL_ID, externalId);
            }
            return updatedClaim;
        } else {
            throw
                new HttpException(String.format(
                    validatedBsDetails,
                    HttpStatus.SC_BAD_REQUEST
                ));
        }

    }

    private String validateBreathingSpaceDetails(BreathingSpace breathingSpace, Claim claim) {
        String validationMessage = null;
        BreathingSpace breathingSpaceInClaim = null;
        Optional<BreathingSpace> breathingSpaceOptional = claim.getClaimData().getBreathingSpace();
        if (breathingSpaceOptional.isPresent()) {
            breathingSpaceInClaim = breathingSpaceOptional.get();
        }

        if (breathingSpaceInClaim != null && breathingSpaceInClaim.getBsType() != null
            && breathingSpace.getBsLiftedFlag().equals("No")) {
            validationMessage = "Breathing Space is already entered for this Claim";
        } else {
            if (breathingSpace.getBsEnteredDateByInsolvencyTeam() != null
                && breathingSpace.getBsEnteredDateByInsolvencyTeam().getYear() == 9999
            ) {
                breathingSpace.setBsEnteredDateByInsolvencyTeam(null);
            }
            if (breathingSpace.getBsExpectedEndDate() != null
                && breathingSpace.getBsExpectedEndDate().getYear() == 9999
            ) {
                breathingSpace.setBsExpectedEndDate(null);
            }
            if (breathingSpace.getBsLiftedDateByInsolvencyTeam() != null
                && breathingSpace.getBsLiftedDateByInsolvencyTeam().getYear() == 9999
            ) {
                breathingSpace.setBsLiftedDateByInsolvencyTeam(null);
            }
            if (breathingSpace.getBsReferenceNumber() != null
                && breathingSpace.getBsReferenceNumber().length() > 16) {
                validationMessage = "The reference number must be maximum of 16 Characters";
            } else if (breathingSpace.getBsEnteredDateByInsolvencyTeam() != null
                && breathingSpace.getBsEnteredDateByInsolvencyTeam().isAfter(LocalDate.now())) {
                validationMessage = "The start date must not be after today's date";
            } else if (breathingSpace.getBsExpectedEndDate() != null
                && breathingSpace.getBsExpectedEndDate().isBefore(LocalDate.now())) {
                validationMessage = "The expected end date must not be before today's date";
            }
        }

        if (claim.getState().equals(TRANSFERRED)
            || claim.getState().equals(BUSINESS_QUEUE)
            || claim.getState().equals(HWF_APPLICATION_PENDING)
            || claim.getState().equals(AWAITING_RESPONSE_HWF)
            || claim.getState().equals(PROCEEDS_IN_CASE_MAN)
            || claim.getState().equals(SETTLED)
            || claim.getState().equals(CLOSED_HWF)) {
            validationMessage = "This Event cannot be triggered since "
                + "the claim is no longer part of the online civil money claims journey";
        }

        return validationMessage;
    }

    public void updatePreferredCourtByClaimReference(String claimNumber) {

        var claim = getClaimByReferenceAnonymous(claimNumber)
            .orElseThrow(() -> new NotFoundException("Claim not found for " + claimNumber));

        if (claim.getPreferredDQCourt().isPresent()) {
            caseRepository.updatePreferredCourtByClaimReference(claim);
        }
    }
}
