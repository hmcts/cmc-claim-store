package uk.gov.hmcts.cmc.claimstore.controllers.support;

import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
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
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.ResponseUtils;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.claimstore.controllers.support.SupportController.AUTHORISATION_IS_REQUIRED;
import static uk.gov.hmcts.cmc.claimstore.controllers.support.SupportController.CLAIM;
import static uk.gov.hmcts.cmc.claimstore.controllers.support.SupportController.CLAIM_DOES_NOT_EXIST;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_RESPONSE;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.ACCEPTATION;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;

@RestController
@ConditionalOnProperty("feature_toggles.staff_emails_enabled")
@RequestMapping("/support")
public class StaffEmailSupportController {

    private final ClaimService claimService;
    private final UserService userService;
    private final DocumentGenerator documentGenerator;
    private final MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler;
    private final DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler;
    private final CCJStaffNotificationHandler ccjStaffNotificationHandler;
    private final AgreementCountersignedStaffNotificationHandler agreementCountersignedStaffNotificationHandler;
    private final ClaimantResponseStaffNotificationHandler claimantResponseStaffNotificationHandler;
    private final PaidInFullStaffNotificationHandler paidInFullStaffNotificationHandler;

    public StaffEmailSupportController(
        ClaimService claimService,
        UserService userService,
        DocumentGenerator documentGenerator,
        MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler,
        DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler,
        CCJStaffNotificationHandler ccjStaffNotificationHandler,
        AgreementCountersignedStaffNotificationHandler agreementCountersignedStaffNotificationHandler,
        ClaimantResponseStaffNotificationHandler claimantResponseStaffNotificationHandler,
        PaidInFullStaffNotificationHandler paidInFullStaffNotificationHandler
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
            .orElseThrow(claimNotFoundException(referenceNumber));

        switch (event) {
            case "claim":
                resendStaffNotificationsOnClaimIssued(claim, authorisation);
                break;
            case "more-time":
                resendStaffNotificationOnMoreTimeRequested(claim);
                break;
            case "response":
                resendStaffNotificationOnDefendantResponseSubmitted(claim, authorisation);
                break;
            case "ccj":
                resendStaffNotificationCCJRequestSubmitted(claim, authorisation);
                break;
            case "settlement":
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

        UserDetails userDetails = userService.getUserDetails(authorisation);
        String fullName = userDetails.getFullName();

        if (!claim.getClaimData().isClaimantRepresented()) {
            GeneratePinResponse pinResponse = userService.generatePin(
                claim.getClaimData().getDefendant().getName(),
                authorisation
            );

            claimService.linkLetterHolder(claim, pinResponse.getUserId(), authorisation);
            documentGenerator.generateForNonRepresentedClaim(
                new CitizenClaimIssuedEvent(claim, pinResponse.getPin(), fullName, authorisation)
            );
        } else {
            documentGenerator.generateForRepresentedClaim(
                new RepresentedClaimIssuedEvent(claim, fullName, authorisation)
            );
        }
    }

    private void resendStaffNotificationForIntentToProceed(Claim claim, String authorization) {
        ClaimantResponse claimantResponse = claim.getClaimantResponse()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_CLAIMANT_RESPONSE));

        if (claimantResponse.getType() != REJECTION) {
            throw new IllegalArgumentException("Rejected Claimant Response is mandatory for 'intent-to-proceed' event");
        }

        claimantResponseStaffNotificationHandler.notifyStaffWithClaimantsIntentionToProceed(
            new ClaimantResponseEvent(claim, authorization)
        );
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
            throw new ConflictException(CLAIM + claim.getReferenceNumber() + " does not have a settlement");
        }
        AgreementCountersignedEvent event = new AgreementCountersignedEvent(claim, null, authorisation);
        agreementCountersignedStaffNotificationHandler.onAgreementCountersigned(event);
    }

    private void resendStaffNotificationClaimantResponse(Claim claim, String authorization) {
        ClaimantResponse claimantResponse = claim.getClaimantResponse()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_CLAIMANT_RESPONSE));
        if (!isSettlementAgreement(claim, claimantResponse)) {
            claimantResponseStaffNotificationHandler.onClaimantResponse(
                new ClaimantResponseEvent(claim, authorization)
            );
        }
    }

    @SuppressWarnings("squid:S2201") // not ignored
    private void resendStaffNotificationForPaidInFull(Claim claim) {
        claim.getMoneyReceivedOn()
            .orElseThrow(() -> new IllegalArgumentException("Claim missing money received on date"));
        paidInFullStaffNotificationHandler.onPaidInFullEvent(new PaidInFullEvent(claim));
    }

    private boolean isSettlementAgreement(Claim claim, ClaimantResponse claimantResponse) {
        Response response = claim.getResponse().orElseThrow(() -> new IllegalStateException(MISSING_RESPONSE));

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

    private Supplier<NotFoundException> claimNotFoundException(String reference) {
        return () -> new NotFoundException(format(CLAIM_DOES_NOT_EXIST, reference));
    }
}
