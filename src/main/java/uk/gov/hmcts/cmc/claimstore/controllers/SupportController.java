package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentRequestedEvent;
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
import uk.gov.hmcts.cmc.domain.models.Claim;

@RestController
@RequestMapping("/support")
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class SupportController {

    private static final String CLAIM = "Claim ";
    private final ClaimService claimService;
    private final UserService userService;
    private final DocumentGenerator documentGenerator;
    private final MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler;
    private final DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler;
    private final CCJStaffNotificationHandler ccjStaffNotificationHandler;
    private final AgreementCountersignedStaffNotificationHandler agreementCountersignedStaffNotificationHandler;

    @Autowired
    public SupportController(
        ClaimService claimService,
        UserService userService,
        DocumentGenerator documentGenerator,
        MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler,
        DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler,
        CCJStaffNotificationHandler ccjStaffNotificationHandler,
        AgreementCountersignedStaffNotificationHandler agreementCountersignedStaffNotificationHandler
    ) {
        this.claimService = claimService;
        this.userService = userService;
        this.documentGenerator = documentGenerator;
        this.moreTimeRequestedStaffNotificationHandler = moreTimeRequestedStaffNotificationHandler;
        this.defendantResponseStaffNotificationHandler = defendantResponseStaffNotificationHandler;
        this.ccjStaffNotificationHandler = ccjStaffNotificationHandler;
        this.agreementCountersignedStaffNotificationHandler = agreementCountersignedStaffNotificationHandler;
    }

    @PutMapping("/claim/{referenceNumber}/event/{event}/resend-staff-notifications")
    @ApiOperation("Resend staff notifications associated with provided event")
    public void resendStaffNotifications(
        @PathVariable("referenceNumber") String referenceNumber,
        @PathVariable("event") String event,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation
    ) throws ServletRequestBindingException {

        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
            .orElseThrow(() -> new NotFoundException(CLAIM + referenceNumber + " does not exist"));

        switch (event) {
            case "claim-issued":
                resendStaffNotificationsOnClaimIssued(claim, authorisation);
                break;
            case "more-time-requested":
                resendStaffNotificationOnMoreTimeRequested(claim);
                break;
            case "response-submitted":
                resendStaffNotificationOnDefendantResponseSubmitted(claim);
                break;
            case "ccj-request-submitted":
                resendStaffNotificationCCJRequestSubmitted(claim, authorisation);
                break;
            case "offer-accepted":
                resendStaffNotificationOnAgreementCountersigned(claim);
                break;
            default:
                throw new NotFoundException("Event " + event + " is not supported");
        }
    }

    private void resendStaffNotificationCCJRequestSubmitted(Claim claim, String authorisation) {
        this.ccjStaffNotificationHandler.onDefaultJudgmentRequestSubmitted(
            new CountyCourtJudgmentRequestedEvent(claim, authorisation)
        );
    }

    private void resendStaffNotificationsOnClaimIssued(Claim claim, String authorisation) {
        if (claim.getDefendantId() != null) {
            throw new ConflictException("Claim has already been linked to defendant - cannot send notification");
        }

        if (!claim.getClaimData().isClaimantRepresented()) {
            GeneratePinResponse pinResponse = userService
                .generatePin(claim.getClaimData().getDefendant().getName(), authorisation);

            claimService.linkLetterHolder(claim.getId(), pinResponse.getUserId());

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

    private void resendStaffNotificationOnDefendantResponseSubmitted(Claim claim) {
        if (!claim.getResponse().isPresent()) {
            throw new ConflictException(CLAIM + claim.getId() + " does not have associated response");
        }
        DefendantResponseEvent event = new DefendantResponseEvent(claim);
        defendantResponseStaffNotificationHandler.onDefendantResponseSubmitted(event);
    }

    private void resendStaffNotificationOnAgreementCountersigned(Claim claim) {
        if (claim.getSettlementReachedAt() == null) {
            throw new ConflictException(CLAIM + claim.getId() + " does not have a settlement");
        }
        AgreementCountersignedEvent event = new AgreementCountersignedEvent(claim, null);
        agreementCountersignedStaffNotificationHandler.onAgreementCountersigned(event);
    }

}
