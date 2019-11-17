package uk.gov.hmcts.cmc.claimstore.controllers.support;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerErrorException;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.claimstore.events.claim.PostClaimOrchestrationHandler;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimSubmissionOperationIndicatorRule;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.IntentionToProceedService;
import uk.gov.hmcts.cmc.claimstore.services.MediationReportService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.MediationRequest;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.hmcts.cmc.domain.utils.ResponseUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isReferredToJudge;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.ACCEPTATION;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;

@RestController
@RequestMapping("/support")
public class SupportController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String CLAIM = "Claim ";
    private static final String CLAIM_DOES_NOT_EXIST = "Claim %s does not exist";
    private static final String AUTHORISATION_IS_REQUIRED = "Authorisation is required";

    private final ClaimService claimService;
    private final UserService userService;
    private final DocumentGenerator documentGenerator;
    private final MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler;
    private final DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler;
    private final CCJStaffNotificationHandler ccjStaffNotificationHandler;
    private final AgreementCountersignedStaffNotificationHandler agreementCountersignedStaffNotificationHandler;
    private final ClaimantResponseStaffNotificationHandler claimantResponseStaffNotificationHandler;
    private final PaidInFullStaffNotificationHandler paidInFullStaffNotificationHandler;
    private final DocumentsService documentsService;
    private final PostClaimOrchestrationHandler postClaimOrchestrationHandler;
    private final MediationReportService mediationReportService;
    private final boolean directionsQuestionnaireEnabled;
    private final ClaimSubmissionOperationIndicatorRule claimSubmissionOperationIndicatorRule;
    private final IntentionToProceedService intentionToProceedService;

    @SuppressWarnings("squid:S00107")
    public SupportController(
            ClaimService claimService,
            UserService userService,
            DocumentGenerator documentGenerator,
            MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler,
            DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler,
            CCJStaffNotificationHandler ccjStaffNotificationHandler,
            AgreementCountersignedStaffNotificationHandler agreementCountersignedStaffNotificationHandler,
            ClaimantResponseStaffNotificationHandler claimantResponseStaffNotificationHandler,
            PaidInFullStaffNotificationHandler paidInFullStaffNotificationHandler,
            DocumentsService documentsService,
            @Autowired(required = false) PostClaimOrchestrationHandler postClaimOrchestrationHandler,
            @Value("${feature_toggles.directions_questionnaire_enabled:false}") boolean directionsQuestionnaireEnabled,
            MediationReportService mediationReportService,
            ClaimSubmissionOperationIndicatorRule claimSubmissionOperationIndicatorRule,
            IntentionToProceedService intentionToProceedService
    ) {
        this.claimService = claimService;
        this.userService = userService;
        this.documentGenerator = documentGenerator;
        this.moreTimeRequestedStaffNotificationHandler = moreTimeRequestedStaffNotificationHandler;
        this.defendantResponseStaffNotificationHandler = defendantResponseStaffNotificationHandler;
        this.ccjStaffNotificationHandler = ccjStaffNotificationHandler;
        this.agreementCountersignedStaffNotificationHandler = agreementCountersignedStaffNotificationHandler;
        this.claimantResponseStaffNotificationHandler = claimantResponseStaffNotificationHandler;
        this.paidInFullStaffNotificationHandler = paidInFullStaffNotificationHandler;
        this.documentsService = documentsService;
        this.postClaimOrchestrationHandler = postClaimOrchestrationHandler;
        this.mediationReportService = mediationReportService;
        this.directionsQuestionnaireEnabled = directionsQuestionnaireEnabled;
        this.claimSubmissionOperationIndicatorRule = claimSubmissionOperationIndicatorRule;
        this.intentionToProceedService = intentionToProceedService;
    }

    @PutMapping("/claim/{referenceNumber}/event/{event}/resend-staff-notifications")
    @ApiOperation("Resend staff notifications associated with provided event")
    public void resendStaffNotifications(
            @PathVariable("referenceNumber") String referenceNumber,
            @PathVariable("event") String event
    ) {
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();

        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
                .orElseThrow(() -> new NotFoundException(String.format(CLAIM_DOES_NOT_EXIST, referenceNumber)));

        switch (event) {
            case "claim-issued":
                resendStaffNotificationsOnClaimIssued(claim, authorisation);
                break;
            case "more-time-requested":
                resendStaffNotificationOnMoreTimeRequested(claim);
                break;
            case "response-submitted":
                resendStaffNotificationOnDefendantResponseSubmitted(claim, authorisation);
                break;
            case "ccj-request-submitted":
                resendStaffNotificationCCJRequestSubmitted(claim, authorisation);
                break;
            case "offer-accepted":
                resendStaffNotificationOnAgreementCountersigned(claim, authorisation);
                break;
            case "claimant-response":
                resendStaffNotificationClaimantResponse(claim, authorisation);
                break;
            case "intent-to-proceed":
                resendStaffNotificationForIntentToProceed(claim, authorisation);
                break;
            case "paid-in-full":
                resendStaffNotificationForPaidInFull(claim);
                break;
            default:
                throw new NotFoundException("Event " + event + " is not supported");
        }
    }

    @PutMapping("/documents/{referenceNumber}/{documentType}")
    @ApiOperation("Ensure a document is available on CCD")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 404, message = "Claim not found"),
            @ApiResponse(code = 500, message = "Unable to upload document")
    })
    @SuppressWarnings("squid:S2201") // orElseThrow does not ignore the result
    public ResponseEntity<?> uploadDocumentToDocumentManagement(
            @PathVariable("referenceNumber") String referenceNumber,
            @PathVariable("documentType") ClaimDocumentType documentType
    ) {
        User caseworker = userService.authenticateAnonymousCaseWorker();

        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
                .orElseThrow(() -> new NotFoundException(String.format(CLAIM_DOES_NOT_EXIST, referenceNumber)));

        if (claim.getClaimDocument(documentType).isPresent()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        documentsService.generateDocument(claim.getExternalId(), documentType, caseworker.getAuthorisation());

        claimService.getClaimByReferenceAnonymous(referenceNumber)
                .orElseThrow(IllegalStateException::new)
                .getClaimDocument(documentType)
                .orElseThrow(() -> new ServerErrorException(
                        "Unable to upload the document. Please try again later",
                        new NotFoundException("Unable to upload the document. Please try again later")
                ));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/claim/{referenceNumber}/reset-operation")
    @ApiOperation("Redo any failed operation. Use the claim submission indicators to indicate the operation to redo.")
    public void resetOperation(
            @PathVariable("referenceNumber") String referenceNumber,
            @RequestBody ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        if (StringUtils.isBlank(authorisation)) {
            throw new BadRequestException(AUTHORISATION_IS_REQUIRED);
        }
        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
                .orElseThrow(() -> new NotFoundException(String.format(CLAIM_DOES_NOT_EXIST, referenceNumber)));

        claimSubmissionOperationIndicatorRule.assertOperationIndicatorUpdateIsValid(claim,
                claimSubmissionOperationIndicators);

        claim = claimService.updateClaimSubmissionOperationIndicators(
                authorisation,
                claim,
                claimSubmissionOperationIndicators
        );
        triggerAsyncOperation(authorisation, claim);
    }

    @PutMapping("/claims/{referenceNumber}/recover-operations")
    @ApiOperation("Recovers the failed operations which are mandatory to issue a claim.")
    public void recoverClaimIssueOperations(@PathVariable("referenceNumber") String referenceNumber) {
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();

        Claim claim = claimService.getClaimByReference(referenceNumber, authorisation)
                .orElseThrow(() -> new NotFoundException(String.format(CLAIM_DOES_NOT_EXIST, referenceNumber)));
        triggerAsyncOperation(authorisation, claim);
    }

    private void triggerAsyncOperation(String authorisation, Claim claim) {
        if (claim.getClaimData().isClaimantRepresented()) {
            String submitterName = claim.getClaimData().getClaimant()
                    .getRepresentative().orElseThrow(IllegalArgumentException::new)
                    .getOrganisationName();

            this.postClaimOrchestrationHandler
                    .representativeIssueHandler(new RepresentedClaimCreatedEvent(claim, submitterName, authorisation));
        } else {
            String submitterName = claim.getClaimData().getClaimant().getName();
            this.postClaimOrchestrationHandler
                    .citizenIssueHandler(new CitizenClaimCreatedEvent(claim, submitterName, authorisation));
        }
    }

    @PostMapping(value = "/sendMediation", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("Generate and Send Mediation Report for Telephone Mediation Service")
    public void sendMediation(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorisation,
            @RequestBody MediationRequest mediationRequest
    ) {
        mediationReportService
                .sendMediationReport(authorisation, mediationRequest.getReportDate());

    }

    @PostMapping(value = "/claims/checkIntentionToProceedDeadline")
    @ApiOperation("Stay claims past their intention proceed deadline")
    public void checkClaimsPastIntentionToProceedDeadline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation,
            @RequestParam(required = false)
            @ApiParam("Optional. If supplied check will run as if triggered at this timestamp. Format is "
                    + "yyyy-MM-ddThh:mm:ss")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime localDateTime) {

        LocalDateTime runDateTime = localDateTime == null ? LocalDateTimeFactory.nowInLocalZone() : localDateTime;

        User user = userService.getUser(authorisation);
        String format = runDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"));
        logger.info(String.format("checkClaimsPastIntentionToProceedDeadline called by %s for date: %s ",
                user.getUserDetails().getId(), format));
        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(runDateTime, user);
    }

    private void resendStaffNotificationCCJRequestSubmitted(Claim claim, String authorisation) {
        this.ccjStaffNotificationHandler.onDefaultJudgmentRequestSubmitted(
                new CountyCourtJudgmentEvent(claim, authorisation)
        );
    }

    private void resendStaffNotificationsOnClaimIssued(Claim claim, String authorisation) {
        if (StringUtils.isBlank(authorisation)) {
            throw new BadRequestException(AUTHORISATION_IS_REQUIRED);
        }

        if (claim.getDefendantId() != null) {
            throw new ConflictException("Claim has already been linked to defendant - cannot send notification");
        }

        if (!claim.getClaimData().isClaimantRepresented()) {
            GeneratePinResponse pinResponse = userService
                    .generatePin(claim.getClaimData().getDefendant().getName(), authorisation);

            String fullName = userService.getUserDetails(authorisation).getFullName();

            claimService.linkLetterHolder(claim, pinResponse.getUserId(), authorisation);

            documentGenerator.generateForNonRepresentedClaim(
                    new CitizenClaimIssuedEvent(claim, pinResponse.getPin(), fullName, authorisation)
            );
        } else {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            documentGenerator.generateForRepresentedClaim(
                    new RepresentedClaimIssuedEvent(claim, userDetails.getFullName(), authorisation)
            );
        }

    }

    private void resendStaffNotificationForIntentToProceed(Claim claim, String authorization) {
        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalArgumentException::new);

        if (!directionsQuestionnaireEnabled) {
            throw new IllegalArgumentException("Direction Question Flag is mandatory for `intent-to-proceed` event");
        }

        if (claimantResponse.getType() != REJECTION) {
            throw new IllegalArgumentException("Rejected Claimant Response is mandatory for `intent-to-proceed` event");
        }

        claimantResponseStaffNotificationHandler
                .notifyStaffWithClaimantsIntentionToProceed(new ClaimantResponseEvent(claim, authorization));
    }

    private void resendStaffNotificationOnMoreTimeRequested(Claim claim) {
        if (!claim.isMoreTimeRequested()) {
            throw new ConflictException("More time has not been requested yet - cannot send notification");
        }

        // Defendant email is not available at this point however it is not used in staff notifications
        MoreTimeRequestedEvent event =
                new MoreTimeRequestedEvent(claim, claim.getResponseDeadline(), null);
        moreTimeRequestedStaffNotificationHandler.sendNotifications(event);
    }

    private void resendStaffNotificationOnDefendantResponseSubmitted(Claim claim, String authorization) {
        if (!claim.getResponse().isPresent()) {
            throw new ConflictException(CLAIM + claim.getReferenceNumber() + " does not have associated response");
        }
        DefendantResponseEvent event = new DefendantResponseEvent(claim, authorization);
        defendantResponseStaffNotificationHandler.onDefendantResponseSubmitted(event);
    }

    private void resendStaffNotificationOnAgreementCountersigned(Claim claim, String authorisation) {
        if (claim.getSettlementReachedAt() == null) {
            throw new ConflictException(CLAIM + claim.getId() + " does not have a settlement");
        }
        AgreementCountersignedEvent event = new AgreementCountersignedEvent(claim, null, authorisation);
        agreementCountersignedStaffNotificationHandler.onAgreementCountersigned(event);
    }

    private void resendStaffNotificationClaimantResponse(Claim claim, String authorization) {
        ClaimantResponse claimantResponse = claim.getClaimantResponse()
                .orElseThrow(IllegalArgumentException::new);
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        if (!isSettlementAgreement(claim, claimantResponse) && (!isReferredToJudge(claimantResponse)
                || (isReferredToJudge(claimantResponse) && PartyUtils.isCompanyOrOrganisation(response.getDefendant())))
        ) {
            claimantResponseStaffNotificationHandler
                    .onClaimantResponse(new ClaimantResponseEvent(claim, authorization));
        }
    }

    @SuppressWarnings("squid:S2201") // not ignored
    private void resendStaffNotificationForPaidInFull(Claim claim) {
        claim.getMoneyReceivedOn().orElseThrow(IllegalArgumentException::new);
        paidInFullStaffNotificationHandler.onPaidInFullEvent(new PaidInFullEvent(claim));
    }

    private boolean isSettlementAgreement(Claim claim, ClaimantResponse claimantResponse) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);

        if (shouldFormaliseResponseAcceptance(response, claimantResponse)) {
            return ((ResponseAcceptation) claimantResponse).getFormaliseOption()
                    .filter(Predicate.isEqual(FormaliseOption.SETTLEMENT)).isPresent();
        }
        return false;
    }

    private boolean shouldFormaliseResponseAcceptance(Response response, ClaimantResponse claimantResponse) {
        return ACCEPTATION == claimantResponse.getType()
                && !ResponseUtils.isResponseStatesPaid(response)
                && !ResponseUtils.isResponsePartAdmitPayImmediately(response);
    }

}
