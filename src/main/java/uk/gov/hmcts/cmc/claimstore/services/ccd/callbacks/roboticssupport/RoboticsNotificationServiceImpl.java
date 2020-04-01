package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsExceptionLogger;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.rpa.DefenceResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.MoreTimeRequestedNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.PaidInFullNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.RequestForJudgementNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class RoboticsNotificationServiceImpl implements RoboticsNotificationService {

    private final UserService userService;

    private final ClaimService claimService;
    private final MoreTimeRequestedNotificationService moreTimeRequestedNotificationService;
    private final DefenceResponseNotificationService defenceResponseNotificationService;
    private final RequestForJudgementNotificationService ccjNotificationService;
    private final PaidInFullNotificationService paidInFullNotificationService;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;

    private final AppInsightsExceptionLogger appInsightsExceptionLogger;
    private final DocumentGenerator documentGenerator;

    @Autowired
    public RoboticsNotificationServiceImpl(
        ClaimService claimService, UserService userService,
        MoreTimeRequestedNotificationService moreTimeRequestedNotificationService,
        DefenceResponseNotificationService defenceResponseNotificationService,
        RequestForJudgementNotificationService ccjNotificationService,
        PaidInFullNotificationService paidInFullNotificationService,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        AppInsightsExceptionLogger appInsightsExceptionLogger,
        DocumentGenerator documentGenerator
    ) {
        this.claimService = claimService;
        this.userService = userService;
        this.moreTimeRequestedNotificationService = moreTimeRequestedNotificationService;
        this.defenceResponseNotificationService = defenceResponseNotificationService;
        this.ccjNotificationService = ccjNotificationService;
        this.paidInFullNotificationService = paidInFullNotificationService;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.appInsightsExceptionLogger = appInsightsExceptionLogger;
        this.documentGenerator = documentGenerator;

    }

    @Override
    public String rpaClaimNotification(String referenceNumber) {
        if (StringUtils.isEmpty(referenceNumber)) {
            throw new BadRequestException("Reference number not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        return resendRPA(referenceNumber, user.getAuthorisation(), reference -> true, claim -> {
            GeneratePinResponse pinResponse = userService
                    .generatePin(claim.getClaimData().getDefendant().getName(), authorisation);

            String fullName = userService.getUserDetails(authorisation).getFullName();
            String userId = pinResponse.getUserId();
            claimService.linkLetterHolder(claim, userId, authorisation);
            documentGenerator.generateForCitizenRPA(
                    new CitizenClaimIssuedEvent(claim, pinResponse.getPin(), fullName, authorisation)
            );
        },
            "Failed to send claim to RPA");
    }

    @Override
    public String rpaMoreTimeNotifications(String referenceNumber) {
        if (StringUtils.isEmpty(referenceNumber)) {
            throw new BadRequestException("Reference number not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        return resendRPA(referenceNumber, user.getAuthorisation(), Claim::isMoreTimeRequested,
            claim -> moreTimeRequestedNotificationService.notifyRobotics(new MoreTimeRequestedEvent(
                claim,
                responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn()),
                claim.getDefendantEmail())),
            "Failed to send more time request to RPA");
    }

    @Override
    public String rpaResponseNotifications(String referenceNumber) {
        if (StringUtils.isEmpty(referenceNumber)) {
            throw new BadRequestException("Reference number not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        return resendRPA(referenceNumber, user.getAuthorisation(),
            claim -> claim.getResponse().isPresent(),
            claim -> defenceResponseNotificationService.notifyRobotics(
                new DefendantResponseEvent(claim, authorisation)),
            "Failed to send response to RPA");
    }

    @Override
    public String rpaCCJNotifications(String referenceNumber) {
        if (StringUtils.isEmpty(referenceNumber)) {
            throw new BadRequestException("Reference number not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        return resendRPA(referenceNumber, user.getAuthorisation(),
            claim -> claim.getCountyCourtJudgment() != null,
            claim -> ccjNotificationService.notifyRobotics(new CountyCourtJudgmentEvent(claim, authorisation)),
            "Failed to send CCJ request to RPA");
    }

    @Override
    public String rpaPIFNotifications(String referenceNumber) {
        if (StringUtils.isEmpty(referenceNumber)) {
            throw new BadRequestException("Reference number not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        return resendRPA(referenceNumber, user.getAuthorisation(),
            claim -> claim.getMoneyReceivedOn().isPresent(),
            claim -> paidInFullNotificationService.notifyRobotics(new PaidInFullEvent(claim)),
            "Failed to send paid-in-full statement to RPA");
    }

    private String resendRPA(
        String reference,
        String authorisation,
        Predicate<Claim> precondition,
        Consumer<Claim> consumer,
        String errorMessage
    ) {

        Optional<Claim> claimOptional = Optional.ofNullable(claimService
            .getClaimByReference(reference, authorisation)
            .orElseThrow(() -> new BadRequestException(RpaStateType.RPA_STATE_MISSING.getValue())));
        Claim claim = claimOptional.filter(precondition)
            .orElseThrow(() -> new BadRequestException(RpaStateType.RPA_STATE_INVALID.getValue()));
        try {
            consumer.accept(claim);
            return RpaStateType.RPA_STATE_SUCCEEDED.getValue();
        } catch (Exception ex) {
            appInsightsExceptionLogger.warn(errorMessage, ex);
            return RpaStateType.RPA_STATE_FAILED.getValue() + ": " + ex.getMessage();
        }
    }
}
