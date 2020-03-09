package uk.gov.hmcts.cmc.claimstore.controllers.support;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.PostClaimOrchestrationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
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
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_REPRESENTATIVE;

@RestController
@RequestMapping("/support")
public class SupportController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String CLAIM = "Claim ";
    public static final String CLAIM_DOES_NOT_EXIST = "Claim %s does not exist";
    public static final String AUTHORISATION_IS_REQUIRED = "Authorisation is required";

    private final ClaimService claimService;
    private final UserService userService;
    private final DocumentsService documentsService;
    private final PostClaimOrchestrationHandler postClaimOrchestrationHandler;
    private final MediationReportService mediationReportService;
    private final ClaimSubmissionOperationIndicatorRule claimSubmissionOperationIndicatorRule;
    private final IntentionToProceedService intentionToProceedService;

    @SuppressWarnings("squid:S00107")
    public SupportController(
        ClaimService claimService,
        UserService userService,
        DocumentsService documentsService,
        PostClaimOrchestrationHandler postClaimOrchestrationHandler,
        MediationReportService mediationReportService,
        ClaimSubmissionOperationIndicatorRule claimSubmissionOperationIndicatorRule,
        IntentionToProceedService intentionToProceedService
    ) {
        this.claimService = claimService;
        this.userService = userService;
        this.documentsService = documentsService;
        this.postClaimOrchestrationHandler = postClaimOrchestrationHandler;
        this.mediationReportService = mediationReportService;
        this.claimSubmissionOperationIndicatorRule = claimSubmissionOperationIndicatorRule;
        this.intentionToProceedService = intentionToProceedService;
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
            .orElseThrow(claimNotFoundException(referenceNumber));

        if (claim.getClaimDocument(documentType).isPresent()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        documentsService.generateDocument(claim.getExternalId(), documentType, caseworker.getAuthorisation());

        // local claim object is now outdated
        claimService.getClaimByReferenceAnonymous(referenceNumber)
            .orElseThrow(() -> new IllegalStateException("Missing claim " + referenceNumber))
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
        @RequestBody ClaimSubmissionOperationIndicators indicators,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        if (StringUtils.isBlank(authorisation)) {
            throw new BadRequestException(AUTHORISATION_IS_REQUIRED);
        }
        Claim claim = claimService.getClaimByReferenceAnonymous(referenceNumber)
            .orElseThrow(claimNotFoundException(referenceNumber));

        claimSubmissionOperationIndicatorRule.assertOperationIndicatorUpdateIsValid(claim, indicators);

        claim = claimService.updateClaimSubmissionOperationIndicators(authorisation, claim, indicators);
        triggerAsyncOperation(authorisation, claim);
    }

    @PutMapping("/claims/{referenceNumber}/recover-operations")
    @ApiOperation("Recovers the failed operations which are mandatory to issue a claim.")
    public void recoverClaimIssueOperations(@PathVariable("referenceNumber") String referenceNumber) {
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();

        Claim claim = claimService.getClaimByReference(referenceNumber, authorisation)
            .orElseThrow(claimNotFoundException(referenceNumber));
        triggerAsyncOperation(authorisation, claim);
    }

    private void triggerAsyncOperation(String authorisation, Claim claim) {
        if (claim.getClaimData().isClaimantRepresented()) {
            String submitterName = claim.getClaimData().getClaimant()
                .getRepresentative()
                .orElseThrow(() -> new IllegalArgumentException(MISSING_REPRESENTATIVE))
                .getOrganisationName();

            this.postClaimOrchestrationHandler.representativeIssueHandler(
                new RepresentedClaimCreatedEvent(claim, submitterName, authorisation)
            );
        } else {
            String submitterName = claim.getClaimData().getClaimant().getName();
            this.postClaimOrchestrationHandler.citizenIssueHandler(
                new CitizenClaimCreatedEvent(claim, submitterName, authorisation)
            );
        }
    }

    @PostMapping(value = "/sendMediation", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Generate and Send Mediation Report for Telephone Mediation Service")
    public void sendMediation(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorisation,
        @RequestBody MediationRequest mediationRequest
    ) {
        mediationReportService.sendMediationReport(authorisation, mediationRequest.getReportDate());

    }

    @PostMapping(value = "/claims/checkIntentionToProceedDeadline")
    @ApiOperation("Stay claims past their intention proceed deadline")
    public void checkClaimsPastIntentionToProceedDeadline(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestParam(required = false)
        @ApiParam("Optional. If supplied check will run as if triggered at this timestamp. Format is ISO 8601")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime localDateTime
    ) {

        LocalDateTime runDateTime = localDateTime == null ? LocalDateTimeFactory.nowInLocalZone() : localDateTime;

        User user = userService.getUser(authorisation);
        String format = runDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"));
        logger.info(format("checkClaimsPastIntentionToProceedDeadline called by %s for date: %s",
            user.getUserDetails().getId(), format));
        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(runDateTime, user);
    }

    private Supplier<NotFoundException> claimNotFoundException(String reference) {
        return () -> new NotFoundException(format(CLAIM_DOES_NOT_EXIST, reference));
    }
}
