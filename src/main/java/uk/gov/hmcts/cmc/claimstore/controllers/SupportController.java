package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferAcceptedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferAcceptedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@RestController
@RequestMapping("/support")
public class SupportController {

    private static final String CLAIM = "Claim ";
    private final ClaimRepository claimRepository;
    private final UserService userService;
    private final ClaimIssuedStaffNotificationHandler claimIssuedStaffNotificationHandler;
    private final MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler;
    private final DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler;
    private final CCJStaffNotificationHandler ccjStaffNotificationHandler;
    private final OfferAcceptedStaffNotificationHandler offerAcceptedStaffNotificationHandler;

    @Autowired
    public SupportController(
        final ClaimRepository claimRepository,
        final UserService userService,
        final ClaimIssuedStaffNotificationHandler claimIssuedStaffNotificationHandler,
        final MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler,
        final DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler,
        final CCJStaffNotificationHandler ccjStaffNotificationHandler,
        final OfferAcceptedStaffNotificationHandler offerAcceptedStaffNotificationHandler
        ) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.claimIssuedStaffNotificationHandler = claimIssuedStaffNotificationHandler;
        this.moreTimeRequestedStaffNotificationHandler = moreTimeRequestedStaffNotificationHandler;
        this.defendantResponseStaffNotificationHandler = defendantResponseStaffNotificationHandler;
        this.ccjStaffNotificationHandler = ccjStaffNotificationHandler;
        this.offerAcceptedStaffNotificationHandler = offerAcceptedStaffNotificationHandler;
    }

    @PutMapping("/claim/{referenceNumber}/event/{event}/resend-staff-notifications")
    @ApiOperation("Resend staff notifications associated with provided event")
    public void resendStaffNotifications(
        @PathVariable("referenceNumber") final String referenceNumber,
        @PathVariable("event") final String event,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) final String authorisation
    ) throws ServletRequestBindingException {

        Claim claim = claimRepository.getByClaimReferenceNumber(referenceNumber)
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
                resendStaffNotificationCCJRequestSubmitted(claim);
                break;
            case "offer-accepted":
                resendStaffNotificationOnOfferAccepted(claim);
                break;
            default:
                throw new NotFoundException("Event " + event + " is not supported");
        }
    }

    private void validateAuthorisationPresentWhenRequired(final String authorisation)
        throws ServletRequestBindingException {
        if (StringUtils.isBlank(authorisation)) {
            throw new ServletRequestBindingException(
                "Missing request header 'Authorization' for method parameter of type String"
            );
        }
    }

    private void resendStaffNotificationCCJRequestSubmitted(final Claim claim) {
        this.ccjStaffNotificationHandler.onDefaultJudgmentRequestSubmitted(
            new CountyCourtJudgmentRequestedEvent(claim)
        );
    }

    private void resendStaffNotificationsOnClaimIssued(final Claim claim, final String authorisation)
        throws ServletRequestBindingException {
        if (claim.getDefendantId() != null) {
            throw new ConflictException("Claim has already been linked to defendant - cannot send notification");
        }

        if (!claim.getClaimData().isClaimantRepresented()) {
            validateAuthorisationPresentWhenRequired(authorisation);

            GeneratePinResponse pinResponse = userService
                .generatePin(claim.getClaimData().getDefendant().getName(), authorisation);

            claimRepository.linkLetterHolder(claim.getId(), pinResponse.getUserId());

            final String fullName = userService.getUserDetails(authorisation).getFullName();

            claimIssuedStaffNotificationHandler
                .onClaimIssued(new ClaimIssuedEvent(claim, pinResponse.getPin(), fullName));

        } else {
            final UserDetails userDetails = userService.getUserDetails(authorisation);

            claimIssuedStaffNotificationHandler
                .onRepresentedClaimIssued(new RepresentedClaimIssuedEvent(claim, userDetails.getFullName()));
        }

    }

    private void resendStaffNotificationOnMoreTimeRequested(final Claim claim) {
        if (!claim.isMoreTimeRequested()) {
            throw new ConflictException("More time has not been requested yet - cannot send notification");
        }

        // Defendant email is not available at this point however it is not used in staff notifications
        MoreTimeRequestedEvent event = new MoreTimeRequestedEvent(claim, claim.getResponseDeadline(), null);
        moreTimeRequestedStaffNotificationHandler.sendNotifications(event);
    }

    private void resendStaffNotificationOnDefendantResponseSubmitted(final Claim claim) {
        if (!claim.getResponse().isPresent()) {
            throw new ConflictException(CLAIM + claim.getId() + " does not have associated response");
        }
        final DefendantResponseEvent event = new DefendantResponseEvent(claim);
        defendantResponseStaffNotificationHandler.onDefendantResponseSubmitted(event);
    }

    private void resendStaffNotificationOnOfferAccepted(final Claim claim) {
        if (claim.getSettlementReachedAt() == null) {
            throw new ConflictException(CLAIM + claim.getId() + " does not have a settlement");
        }
        final OfferAcceptedEvent event = new OfferAcceptedEvent(claim, null);
        offerAcceptedStaffNotificationHandler.onOfferAccepted(event);
    }

}
