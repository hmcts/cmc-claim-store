package uk.gov.hmcts.cmc.claimstore.controllers.support;

import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.*;
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
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.ArrayList;
import java.util.List;

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
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorisation) {

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
            throw new BadRequestException("Authorisation is required");
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

    private void resendClaimsToRPA(List<Claim> claims, String authorisation) {
        if (StringUtils.isBlank(authorisation)) {
            throw new BadRequestException("Authorisation is required");
        }

        for (Claim claim: claims) {
            GeneratePinResponse pinResponse = userService
                .generatePin(claim.getClaimData().getDefendant().getName(), authorisation);

            String fullName = userService.getUserDetails(authorisation).getFullName();

            documentGenerator.generateForCitizenRPA(
                new CitizenClaimIssuedEvent(claim, pinResponse.getPin(), fullName, authorisation)
            );
        }
    }

    private List<Claim> checkClaimsExist(List<String> referenceNumbers) {
        List<Claim> claims  = new ArrayList<>();
        for (String referenceNumber: referenceNumbers) {
            Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
                .orElseThrow(() -> new NotFoundException(CLAIM + referenceNumber + " does not exist"));

            claims.add(claim);
        }
        return claims;
    }

}
