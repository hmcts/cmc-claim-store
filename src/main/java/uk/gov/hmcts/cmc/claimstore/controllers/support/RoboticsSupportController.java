package uk.gov.hmcts.cmc.claimstore.controllers.support;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsExceptionLogger;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.rpa.ClaimantResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.DefenceResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.MoreTimeRequestedNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.PaidInFullNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.RequestForJudgmentNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toMap;

@RestController
@RequestMapping("/support/rpa")
public class RoboticsSupportController {
    private static final String RPA_STATE_MISSING = "missing";
    private static final String RPA_STATE_INVALID = "invalid";
    private static final String RPA_STATE_FAILED = "failed";
    private static final String RPA_STATE_SUCCEEDED = "succeeded";

    private final UserService userService;

    private final ClaimService claimService;
    private final MoreTimeRequestedNotificationService moreTimeRequestedNotificationService;
    private final DefenceResponseNotificationService defenceResponseNotificationService;
    private final ClaimantResponseNotificationService claimantResponseNotificationService;
    private final RequestForJudgmentNotificationService ccjNotificationService;
    private final PaidInFullNotificationService paidInFullNotificationService;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;

    private final AppInsightsExceptionLogger appInsightsExceptionLogger;
    private final DocumentGenerator documentGenerator;

    @Autowired
    public RoboticsSupportController(
        ClaimService claimService,
        UserService userService,
        MoreTimeRequestedNotificationService moreTimeRequestedNotificationService,
        DefenceResponseNotificationService defenceResponseNotificationService,
        ClaimantResponseNotificationService claimantResponseNotificationService,
        RequestForJudgmentNotificationService ccjNotificationService,
        PaidInFullNotificationService paidInFullNotificationService,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        AppInsightsExceptionLogger appInsightsExceptionLogger,
        DocumentGenerator documentGenerator
    ) {
        this.claimService = claimService;
        this.userService = userService;
        this.moreTimeRequestedNotificationService = moreTimeRequestedNotificationService;
        this.defenceResponseNotificationService = defenceResponseNotificationService;
        this.claimantResponseNotificationService = claimantResponseNotificationService;
        this.ccjNotificationService = ccjNotificationService;
        this.paidInFullNotificationService = paidInFullNotificationService;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.appInsightsExceptionLogger = appInsightsExceptionLogger;
        this.documentGenerator = documentGenerator;
    }

    @PutMapping("/claim")
    @ApiOperation("Send RPA notifications for multiple claims")
    public Map<String, String> rpaClaimNotifications(@RequestBody List<String> referenceNumbers) {
        if (referenceNumbers == null || referenceNumbers.isEmpty()) {
            throw new IllegalArgumentException("Reference numbers not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        return referenceNumbers.stream()
            .collect(toMap(Function.identity(), ref -> resendRPA(
                ref,
                user.getAuthorisation(),
                reference -> true,
                claim -> {
                    GeneratePinResponse pinResponse = userService
                        .generatePin(claim.getClaimData().getDefendant().getName(), authorisation);

                    String fullName = userService.getUserDetails(authorisation).getFullName();

                    String userId = pinResponse.getUserId();
                    claimService.linkLetterHolder(claim, userId, authorisation);

                    documentGenerator.generateForCitizenRPA(
                        new CitizenClaimIssuedEvent(claim, pinResponse.getPin(), fullName, authorisation)
                    );
                },
                "Failed to send claim to RPA"
            )));
    }

    @PutMapping("/more-time")
    @ApiOperation("Send RPA notifications for multiple more-time requests")
    public Map<String, String> rpaMoreTimeNotifications(@RequestBody List<String> referenceNumbers) {
        if (referenceNumbers == null || referenceNumbers.isEmpty()) {
            throw new IllegalArgumentException("Reference numbers not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        return referenceNumbers.stream()
            .collect(toMap(Function.identity(), ref -> resendRPA(
                ref,
                user.getAuthorisation(),
                Claim::isMoreTimeRequested,
                claim -> moreTimeRequestedNotificationService.notifyRobotics(new MoreTimeRequestedEvent(
                    claim,
                    responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn()),
                    claim.getDefendantEmail())),
                "Failed to send more time request to RPA"
            )));
    }

    @PutMapping("/response")
    @ApiOperation("Send RPA notifications for multiple responses")
    public Map<String, String> rpaResponseNotifications(@RequestBody List<String> referenceNumbers) {
        if (referenceNumbers == null || referenceNumbers.isEmpty()) {
            throw new IllegalArgumentException("Reference numbers not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        return referenceNumbers.stream()
            .collect(toMap(Function.identity(), ref -> resendRPA(
                ref,
                authorisation,
                claim -> claim.getResponse().isPresent(),
                claim -> defenceResponseNotificationService.notifyRobotics(
                    new DefendantResponseEvent(claim, authorisation)),
                "Failed to send response to RPA"
            )));
    }

    @PutMapping("/claimant-response")
    @ApiOperation("Send RPA notifications for multiple responses")
    public Map<String, String> rpaClaimantResponseNotifications(@RequestBody List<String> referenceNumbers) {
        if (referenceNumbers == null || referenceNumbers.isEmpty()) {
            throw new IllegalArgumentException("Reference numbers not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        return referenceNumbers.stream()
            .collect(toMap(Function.identity(), ref -> resendRPA(
                ref,
                authorisation,
                claim -> claim.getResponse().isPresent(),
                claim -> claimantResponseNotificationService.notifyRobotics(
                    new ClaimantResponseEvent(claim, authorisation)),
                "Failed to send response to RPA"
            )));
    }

    @PutMapping("/ccj")
    @ApiOperation("Send RPA notifications for multiple CCJ requests")
    public Map<String, String> rpaCCJNotifications(@RequestBody List<String> referenceNumbers) {
        if (referenceNumbers == null || referenceNumbers.isEmpty()) {
            throw new IllegalArgumentException("Reference numbers not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        return referenceNumbers.stream()
            .collect(toMap(Function.identity(), ref -> resendRPA(
                ref,
                authorisation,
                claim -> claim.getCountyCourtJudgment() != null,
                claim -> ccjNotificationService.notifyRobotics(new CountyCourtJudgmentEvent(claim, authorisation)),
                "Failed to send CCJ request to RPA"
            )));
    }

    @PutMapping("/paid-in-full")
    @ApiOperation("Send RPA notifications for multiple paid-in-full events")
    public Map<String, String> rpaPIFNotifications(@RequestBody List<String> referenceNumbers) {
        if (referenceNumbers == null || referenceNumbers.isEmpty()) {
            throw new IllegalArgumentException("Reference numbers not supplied");
        }
        User user = userService.authenticateAnonymousCaseWorker();
        return referenceNumbers.stream()
            .collect(toMap(Function.identity(), ref -> resendRPA(
                ref,
                user.getAuthorisation(),
                claim -> claim.getMoneyReceivedOn().isPresent(),
                claim -> paidInFullNotificationService.notifyRobotics(new PaidInFullEvent(claim)),
                "Failed to send paid-in-full statement to RPA"
            )));
    }

    private String resendRPA(
        String reference,
        String authorisation,
        Predicate<Claim> precondition,
        Consumer<Claim> consumer,
        String errorMessage
    ) {
        Optional<Claim> claimOptional = claimService.getClaimByReference(reference, authorisation);
        if (!claimOptional.isPresent()) {
            return RPA_STATE_MISSING;
        }
        Claim claim = claimOptional.get();
        if (!precondition.test(claim)) {
            return RPA_STATE_INVALID;
        }
        try {
            consumer.accept(claim);
            return RPA_STATE_SUCCEEDED;
        } catch (Exception ex) {
            appInsightsExceptionLogger.warn(errorMessage, ex);
            return RPA_STATE_FAILED + ": " + ex.getMessage();
        }
    }
}
