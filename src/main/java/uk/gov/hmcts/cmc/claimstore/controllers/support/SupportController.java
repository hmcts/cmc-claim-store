package uk.gov.hmcts.cmc.claimstore.controllers.support;

import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/support")
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class SupportController {

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
    private final DocumentsService documentsService;

    @SuppressWarnings("squid:S00107")
    @Autowired
    public SupportController(
        ClaimService claimService,
        UserService userService,
        DocumentGenerator documentGenerator,
        MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler,
        DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler,
        CCJStaffNotificationHandler ccjStaffNotificationHandler,
        AgreementCountersignedStaffNotificationHandler agreementCountersignedStaffNotificationHandler,
        DocumentsService documentsService
    ) {
        this.claimService = claimService;
        this.userService = userService;
        this.documentGenerator = documentGenerator;
        this.moreTimeRequestedStaffNotificationHandler = moreTimeRequestedStaffNotificationHandler;
        this.defendantResponseStaffNotificationHandler = defendantResponseStaffNotificationHandler;
        this.ccjStaffNotificationHandler = ccjStaffNotificationHandler;
        this.agreementCountersignedStaffNotificationHandler = agreementCountersignedStaffNotificationHandler;
        this.documentsService = documentsService;
    }

    @PutMapping("/claim/{referenceNumber}/event/{event}/resend-staff-notifications")
    @ApiOperation("Resend staff notifications associated with provided event")
    public void resendStaffNotifications(
        @PathVariable("referenceNumber") String referenceNumber,
        @PathVariable("event") String event,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorisation) {

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
            default:
                throw new NotFoundException("Event " + event + " is not supported");
        }
    }

    @PutMapping("/claim/{referenceNumber}/claimDocumentType/{claimDocumentType}/uploadDocumentToDocumentManagement")
    @ApiOperation("Upload document to document Management")
    public void uploadDocumentToDocumentManagement(
        @PathVariable("referenceNumber") String referenceNumber,
        @PathVariable("claimDocumentType") ClaimDocumentType claimDocumentType,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation) {

        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
            .orElseThrow(() -> new NotFoundException(String.format(CLAIM_DOES_NOT_EXIST, referenceNumber)));
        if (StringUtils.isBlank(authorisation)) {
            throw new BadRequestException(AUTHORISATION_IS_REQUIRED);
        }
        switch (claimDocumentType) {
            case SEALED_CLAIM:
                documentsService.generateSealedClaim(claim.getExternalId(), authorisation);
                break;
            case CLAIM_ISSUE_RECEIPT:
                documentsService.generateClaimIssueReceipt(claim.getExternalId(), authorisation);
                break;
            case DEFENDANT_RESPONSE_RECEIPT:
                documentsService.generateDefendantResponseReceipt(claim.getExternalId(), authorisation);
                break;
            case CCJ_REQUEST:
                documentsService.generateCountyCourtJudgement(claim.getExternalId(), authorisation);
                break;
            case SETTLEMENT_AGREEMENT:
                documentsService.generateSettlementAgreement(claim.getExternalId(), authorisation);
                break;
            default:
                throw new BadRequestException("ClaimDocumentType " + claimDocumentType + " is not supported");
        }
        claimService.getClaimByReferenceAnonymous(referenceNumber)
            .ifPresent(updatedClaim -> updatedClaim.getClaimDocument(claimDocumentType)
                .orElseThrow(() -> new NotFoundException("Unable to upload the document. Please try again later")));
    }

    @PutMapping("/claim/resend-rpa-notifications")
    @ApiOperation("Resend notifications for multiple citizen claims")
    public void resendRPANotifications(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorisation,
        @RequestBody List<String> referenceNumbers) {
        if (referenceNumbers.isEmpty()) {
            throw new IllegalArgumentException("Reference numbers not supplied");
        }
        List<Claim> existingClaims = checkClaimsExist(referenceNumbers);
        resendClaimsToRPA(existingClaims, authorisation);
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

    private void resendStaffNotificationOnMoreTimeRequested(Claim claim) {
        if (!claim.isMoreTimeRequested()) {
            throw new ConflictException("More time has not been requested yet - cannot send notification");
        }

        // Defendant email is not available at this point however it is not used in staff notifications
        MoreTimeRequestedEvent event = new MoreTimeRequestedEvent(claim, claim.getResponseDeadline(), null);
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

    private void resendClaimsToRPA(List<Claim> claims, String authorisation) {
        if (StringUtils.isBlank(authorisation)) {
            throw new BadRequestException(AUTHORISATION_IS_REQUIRED);
        }

        for (Claim claim : claims) {
            GeneratePinResponse pinResponse = userService
                .generatePin(claim.getClaimData().getDefendant().getName(), authorisation);

            String fullName = userService.getUserDetails(authorisation).getFullName();

            documentGenerator.generateForCitizenRPA(
                new CitizenClaimIssuedEvent(claim, pinResponse.getPin(), fullName, authorisation)
            );
        }
    }

    private List<Claim> checkClaimsExist(List<String> referenceNumbers) {
        List<Claim> claims = new ArrayList<>();
        for (String referenceNumber : referenceNumbers) {
            Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
                .orElseThrow(() -> new NotFoundException(String.format(CLAIM_DOES_NOT_EXIST, referenceNumber)));

            claims.add(claim);
        }
        return claims;
    }

}
