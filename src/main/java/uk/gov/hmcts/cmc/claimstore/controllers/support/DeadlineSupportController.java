package uk.gov.hmcts.cmc.claimstore.controllers.support;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DateUtils.DATE_OF_5_0_0_RELEASE;

@RestController
@RequestMapping("/support/deadline")
public class DeadlineSupportController {
    private static final Predicate<Claim> CREATED_BEFORE_5_0_0 = claim -> claim.getCreatedAt()
        .isBefore(DATE_OF_5_0_0_RELEASE);
    private static final Predicate<Claim> HAS_DQ_FEATURE = claim -> claim.getFeatures() != null
        && claim.getFeatures().contains("directionsQuestionnaire");
    private static final Predicate<Claim> HAS_DQ_DEADLINE = claim -> claim.getDirectionsQuestionnaireDeadline() != null;
    private static final Predicate<Claim> UNANSWERED = claim -> !claim.getResponse().isPresent();
    private static final Predicate<Claim> DEFENDANT_MEDIATION = claim -> claim.getResponse()
        .flatMap(Response::getFreeMediation)
        .filter(YesNoOption.YES::equals)
        .isPresent();
    private static final Predicate<Claim> FULL_ADMIT_RESPONSE = claim -> claim.getResponse()
        .map(Response::getResponseType)
        .filter(ResponseType.FULL_ADMISSION::equals)
        .isPresent();
    private static final Predicate<Claim> CLAIMANT_ACCEPTED = claim -> claim.getClaimantResponse()
        .map(ClaimantResponse::getType)
        .filter(ClaimantResponseType.ACCEPTATION::equals)
        .isPresent();
    private static final Predicate<Claim> CLAIMANT_MEDIATION = claim -> claim.getClaimantResponse()
        .filter(claimantResponse -> ClaimantResponseType.REJECTION.equals(claimantResponse.getType()))
        .map(ResponseRejection.class::cast)
        .flatMap(ResponseRejection::getFreeMediation)
        .filter(YesNoOption.YES::equals)
        .isPresent();

    private final UserService userService;
    private final ClaimService claimService;
    private final DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;
    private final CaseRepository caseRepository;

    public DeadlineSupportController(
        UserService userService,
        ClaimService claimService,
        DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator,
        CaseRepository caseRepository
    ) {
        requireNonNull(userService);
        requireNonNull(claimService);
        requireNonNull(directionsQuestionnaireDeadlineCalculator);
        requireNonNull(caseRepository);

        this.userService = userService;
        this.claimService = claimService;
        this.directionsQuestionnaireDeadlineCalculator = directionsQuestionnaireDeadlineCalculator;
        this.caseRepository = caseRepository;
    }

    @PutMapping("/{deadlineType}/claim/{referenceNumber}")
    @ApiOperation("Calculate and define a deadline for a claim")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Deadline already defined"),
        @ApiResponse(code = 201, message = "Deadline calculated and defined"),
        @ApiResponse(code = 403, message = "Claim is in an invalid state for this deadline")
    })
    public ResponseEntity<String> defineDeadline(
        @PathVariable String deadlineType,
        @PathVariable String referenceNumber,
        @RequestParam(name = "overwrite", required = false, defaultValue = "false") boolean overwrite
    ) {
        String authorisation = userService.authenticateAnonymousCaseWorker().getAuthorisation();
        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
            .orElseThrow(() -> new NotFoundException(format("Claim %s does not exist", referenceNumber)));

        if ("dq".equals(deadlineType)) {
            return defineDQDeadline(claim, authorisation, overwrite);
        }

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body("Unrecognised deadline type: " + deadlineType);
    }

    private ResponseEntity<String> defineDQDeadline(Claim claim, String authorisation, boolean overwrite) {
        // if there's already a DQ deadline - return 200
        if (!overwrite && HAS_DQ_DEADLINE.test(claim)) {
            return ResponseEntity.ok(format("Claim %s already has a directions questionnaire deadline of %s.",
                claim.getReferenceNumber(), claim.getDirectionsQuestionnaireDeadline()));
        }

        // if the claim has the online DQ feature it is forbidden to define a DQ deadline
        if (HAS_DQ_FEATURE.test(claim)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(format("Claim %s has online DQs enabled; "
                + "cannot define a directions questionnaire deadline", claim.getReferenceNumber()));
        }

        // if there's no response to this claim it is forbidden to define a DQ deadline
        if (UNANSWERED.test(claim)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(format("Claim %s does not have a response; "
                + "cannot define a directions questionnaire deadline", claim.getReferenceNumber()));
        }

        if (CREATED_BEFORE_5_0_0.test(claim)) {
            return defineDQDeadlinePre_5_0_0(claim, authorisation);
        } else {
            return defineDQDeadlinePost_5_0_0(claim, authorisation);
        }
    }

    private ResponseEntity<String> defineDQDeadlinePre_5_0_0(Claim claim, String authorisation) {
        // if the defendant agreed to mediation on a pre-5.0.0 claim it is forbidden to define a DQ deadline
        if (DEFENDANT_MEDIATION.test(claim)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(format("Claim %s is from before 5.0.0 and "
                    + "its defendant agreed to mediation; cannot define a directions questionnaire deadline.",
                claim.getReferenceNumber()));
        }

        // if the defendant fully admits a pre-5.0.0 claim it is forbidden to define a DQ deadline
        if (FULL_ADMIT_RESPONSE.test(claim)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(format("Claim %s is from before 5.0.0 and "
                    + "its defendant fully admitted; cannot define a directions questionnaire deadline.",
                claim.getReferenceNumber()));
        }

        // if the defendant disputed any/all of the claim and did not want mediation for a pre-5.0.0 claim
        // then we can define a directions questionnaire deadline
        LocalDate deadline = updateDeadline(claim, authorisation, claim.getRespondedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(format("Claim %s has been been assigned a "
            + "directions questionnaire deadline of %s.", claim.getReferenceNumber(), deadline));
    }

    private LocalDate updateDeadline(Claim claim, String authorisation, LocalDateTime respondedDate) {
        LocalDate deadline = directionsQuestionnaireDeadlineCalculator
            .calculateDirectionsQuestionnaireDeadline(respondedDate);
        caseRepository.updateDirectionsQuestionnaireDeadline(claim, deadline, authorisation);
        return deadline;
    }

    private ResponseEntity<String> defineDQDeadlinePost_5_0_0(Claim claim, String authorisation) {
        // if there's no claimant response to this claim it is forbidden to define a DQ deadline
        Optional<LocalDateTime> optionalClaimantResponseTime = claim.getClaimantRespondedAt();
        if (!optionalClaimantResponseTime.isPresent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(format("Claim %s does not have a claimant "
                + "response; cannot define a directions questionnaire deadline", claim.getReferenceNumber()));
        }

        // if the claimant accepted the defence to this claim, it is forbidden to define a DQ deadline
        if (CLAIMANT_ACCEPTED.test(claim)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(format("Claim %s has an acceptation "
                + "claimant response; cannot define a directions questionnaire deadline.", claim.getReferenceNumber()));
        }

        // if the claimant agreed to mediation on this claim, it is forbidden to define a DQ deadline
        if (CLAIMANT_MEDIATION.test(claim)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(format("Claim %s has a mediation agreement; "
                + "cannot define a directions questionnaire deadline.", claim.getReferenceNumber()));
        }

        LocalDate deadline = updateDeadline(claim, authorisation, optionalClaimantResponseTime.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(format("Claim %s has been been assigned a "
            + "directions questionnaire deadline of %s.", claim.getReferenceNumber(), deadline));
    }
}
