package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.rules.PaidInFullRule;
import uk.gov.hmcts.cmc.claimstore.utils.CCDCaseDataToClaim;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.response.CaseReference;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_LEGAL;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_MORE_TIME_REQUESTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_MORE_TIME_REQUESTED_PAPER;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Component
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final IssueDateCalculator issueDateCalculator;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;
    private final UserService userService;
    private final EventProducer eventProducer;
    private final CaseRepository caseRepository;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final AppInsights appInsights;
    private final CCDCaseDataToClaim ccdCaseDataToClaim;
    private final PaidInFullRule paidInFullRule;
    private CCDEventProducer ccdEventProducer;

    @SuppressWarnings("squid:S00107") //Constructor need all parameters
    @Autowired
    public ClaimService(
        ClaimRepository claimRepository,
        CaseRepository caseRepository,
        UserService userService,
        IssueDateCalculator issueDateCalculator,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        EventProducer eventProducer,
        AppInsights appInsights,
        CCDCaseDataToClaim ccdCaseDataToClaim,
        PaidInFullRule paidInFullRule,
        CCDEventProducer ccdEventProducer) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.issueDateCalculator = issueDateCalculator;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.eventProducer = eventProducer;
        this.caseRepository = caseRepository;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.appInsights = appInsights;
        this.ccdCaseDataToClaim = ccdCaseDataToClaim;
        this.directionsQuestionnaireDeadlineCalculator = directionsQuestionnaireDeadlineCalculator;
        this.paidInFullRule = paidInFullRule;
        this.ccdEventProducer = ccdEventProducer;
    }

    public Claim getClaimById(long claimId) {
        return claimRepository
            .getById(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + claimId));
    }

    public List<Claim> getClaimBySubmitterId(String submitterId, String authorisation) {
        return caseRepository.getBySubmitterId(submitterId, authorisation);
    }

    public Claim getClaimByLetterHolderId(String id, String authorisation) {
        return caseRepository
            .getByLetterHolderId(id, authorisation)
            .orElseThrow(() -> new NotFoundException("Claim not found for letter holder id " + id));
    }

    public Claim getClaimByExternalId(String externalId, String authorisation) {
        return caseRepository
            .getClaimByExternalId(externalId, authorisation)
            .orElseThrow(() -> new NotFoundException("Claim not found by external id " + externalId));
    }

    public Optional<Claim> getClaimByReference(String reference, String authorisation) {
        return caseRepository
            .getByClaimReferenceNumber(reference, authorisation);
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
        return claimRepository.getByExternalReference(externalReference, submitterId);
    }

    public List<Claim> getClaimByDefendantId(String id, String authorisation) {
        return caseRepository.getByDefendantId(id, authorisation);
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

    public CaseReference savePrePayment(String externalId, String authorisation) {
        CaseReference caseReference = caseRepository.savePrePaymentClaim(externalId, authorisation);
        ccdEventProducer.createCCDPrePaymentEvent(externalId, authorisation);
        return caseReference;
    }

    @Transactional(transactionManager = "transactionManager")
    public Claim saveClaim(
        String submitterId,
        ClaimData claimData,
        String authorisation,
        List<String> features
    ) {
        String externalId = claimData.getExternalId().toString();

        Long prePaymentClaimId = caseRepository.getOnHoldIdByExternalId(externalId, authorisation);

        LocalDateTime now = LocalDateTimeFactory.nowInLocalZone();
        Optional<GeneratePinResponse> pinResponse = Optional.empty();

        if (!claimData.isClaimantRepresented()) {
            pinResponse = Optional.of(userService.generatePin(claimData.getDefendant().getName(), authorisation));
        }

        Optional<String> letterHolderId = pinResponse.map(GeneratePinResponse::getUserId);
        LocalDate issuedOn = issueDateCalculator.calculateIssueDay(now);
        LocalDate responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(issuedOn);
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String submitterEmail = userDetails.getEmail();

        Claim claim = Claim.builder()
            .id(prePaymentClaimId)
            .claimData(claimData)
            .submitterId(submitterId)
            .issuedOn(issuedOn)
            .responseDeadline(responseDeadline)
            .externalId(externalId)
            .submitterEmail(submitterEmail)
            .createdAt(nowInUTC())
            .letterHolderId(letterHolderId.orElse(null))
            .features(features)
            .build();

        Claim issuedClaim = caseRepository.saveClaim(authorisation, claim);

        eventProducer.createClaimIssuedEvent(
            issuedClaim,
            pinResponse.map(GeneratePinResponse::getPin).orElse(null),
            userDetails.getFullName(),
            authorisation
        );

        Claim retrievedClaim = getClaimByExternalId(externalId, authorisation);
        trackClaimIssued(retrievedClaim.getReferenceNumber(), retrievedClaim.getClaimData().isClaimantRepresented());
        ccdEventProducer.createCCDClaimIssuedEvent(retrievedClaim, authorisation);

        return retrievedClaim;
    }

    private void trackClaimIssued(String referenceNumber, boolean represented) {
        AppInsightsEvent event = represented ? CLAIM_ISSUED_LEGAL : CLAIM_ISSUED_CITIZEN;
        appInsights.trackEvent(event, REFERENCE_NUMBER, referenceNumber);
    }

    public Claim requestMoreTimeForResponse(String externalId, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);

        this.moreTimeRequestRule.assertMoreTimeCanBeRequested(claim);

        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

        caseRepository.requestMoreTimeForResponse(authorisation, claim, newDeadline);

        claim = getClaimByExternalId(externalId, authorisation);
        UserDetails defendant = userService.getUserDetails(authorisation);
        eventProducer.createMoreTimeForResponseRequestedEvent(claim, newDeadline, defendant.getEmail());
        ccdEventProducer.createMoreTimeForCCDResponseRequestedEvent(authorisation, externalId, newDeadline);

        appInsights.trackEvent(RESPONSE_MORE_TIME_REQUESTED, REFERENCE_NUMBER, claim.getReferenceNumber());
        return claim;
    }

    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse requestMoreTimeOnPaper(
        CallbackRequest callbackRequest,
        boolean validateOnly
    ) {
        Claim claim = convertCallbackToClaim(callbackRequest);

        List<String> validationResult = this.moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);
        AboutToStartOrSubmitCallbackResponseBuilder builder = AboutToStartOrSubmitCallbackResponse
            .builder();

        if (validateOnly || !validationResult.isEmpty()) {
            return builder
                .errors(validationResult)
                .build();
        }

        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

        Map<String, Object> data = new HashMap<>(((Map<String, Object>) callbackRequest.getCaseDetails()
            .get("case_data"))
        );
        data.put("moreTimeRequested", CCDYesNoOption.YES);
        data.put("responseDeadline", newDeadline);

        return builder
            .data(data)
            .build();
    }

    public SubmittedCallbackResponse requestMoreTimeOnPaperSubmitted(CallbackRequest callbackRequest) {
        Claim claim = convertCallbackToClaim(callbackRequest);

        eventProducer.createMoreTimeForResponseRequestedEvent(
            claim,
            claim.getResponseDeadline(),
            claim.getClaimData().getDefendant().getEmail().orElse(null)
        );
        appInsights.trackEvent(RESPONSE_MORE_TIME_REQUESTED_PAPER, REFERENCE_NUMBER, claim.getReferenceNumber());

        return SubmittedCallbackResponse.builder()
            .build();
    }

    @SuppressWarnings("unchecked")
    private Claim convertCallbackToClaim(CallbackRequest caseDetails) {
        return ccdCaseDataToClaim.to(
            (long) caseDetails.getCaseDetails().get("id"),
            (Map<String, Object>) caseDetails.getCaseDetails().get("case_data")
        );
    }

    public void linkDefendantToClaim(String authorisation) {
        caseRepository.linkDefendant(authorisation);
        ccdEventProducer.linkDefendantCCDEvent(authorisation);
    }

    public void linkSealedClaimDocument(String authorisation, Claim claim, URI sealedClaimDocument) {
        caseRepository.linkSealedClaimDocument(authorisation, claim, sealedClaimDocument);
        ccdEventProducer.linkSealedClaimDocumentCCDEvent(authorisation, claim, sealedClaimDocument);
    }

    public void linkLetterHolder(Long claimId, String userId) {
        claimRepository.linkLetterHolder(claimId, userId);
    }

    public void saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment
    ) {
        caseRepository.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);
        ccdEventProducer.createCCDCountyCourtJudgmentEvent(claim, authorisation, countyCourtJudgment);
        appInsights.trackEvent(CCJ_REQUESTED, REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    public void saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        String authorization
    ) {
        caseRepository.saveDefendantResponse(claim, defendantEmail, response, authorization);
        if (isFullDefenceWithNoMediation(response)) {
            LocalDate deadline = directionsQuestionnaireDeadlineCalculator
                .calculateDirectionsQuestionnaireDeadlineCalculator(LocalDateTime.now());
            caseRepository.updateDirectionsQuestionnaireDeadline(claim, deadline, authorization);
        }
    }

    public Claim paidInFull(String externalId, PaidInFull paidInFull, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        String claimantId = userService.getUserDetails(authorisation).getId();
        this.paidInFullRule.assertPaidInFull(claim, claimantId);
        this.caseRepository.paidInFull(claim, paidInFull, authorisation);
        Claim updatedClaim = getClaimByExternalId(externalId, authorisation);
        this.eventProducer.createPaidInFullEvent(updatedClaim);
        this.ccdEventProducer.createCCDPaidInFullEvent(authorisation, claim, paidInFull);
        return updatedClaim;
    }

    private static boolean isFullDefenceWithNoMediation(Response response) {
        return response.getResponseType().equals(ResponseType.FULL_DEFENCE)
            && response.getFreeMediation().filter(Predicate.isEqual(YesNoOption.NO)).isPresent();
    }

    public void saveReDetermination(
        String authorisation,
        Claim claim,
        ReDetermination redetermination,
        String submitterId
    ) {
        caseRepository.saveReDetermination(authorisation, claim, redetermination, submitterId);
    }
}
