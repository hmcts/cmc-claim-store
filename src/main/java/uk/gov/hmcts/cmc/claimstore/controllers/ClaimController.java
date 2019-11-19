package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.ioc.CreatePaymentResponse;
import uk.gov.hmcts.cmc.domain.models.response.DefendantLinkStatus;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.CLAIM_REFERENCE_PATTERN;
import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.UUID_PATTERN;

@Api
@RestController
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ClaimController {

    private final ClaimService claimService;

    @Autowired
    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping("/claimant/{submitterId}")
    @ApiOperation("Fetch user claims for given submitter id")
    public List<Claim> getBySubmitterId(@PathVariable("submitterId") String submitterId,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        return claimService.getClaimBySubmitterId(submitterId, authorisation);
    }

    @GetMapping("/letter/{letterHolderId}")
    @ApiOperation("Fetch user claim for given letter holder id")
    public Claim getByLetterHolderId(
        @PathVariable("letterHolderId") String letterHolderId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.getClaimByLetterHolderId(letterHolderId, authorisation);
    }

    @GetMapping("/{externalId:" + UUID_PATTERN + "}")
    @ApiOperation("Fetch claim for given external id")
    public Claim getByExternalId(@PathVariable("externalId") String externalId,
                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        return claimService.getClaimByExternalId(externalId, authorisation);
    }

    @GetMapping("/{claimReference:" + CLAIM_REFERENCE_PATTERN + "}")
    @ApiOperation("Fetch claim for given claim reference")
    public Claim getByClaimReference(@PathVariable("claimReference") String claimReference,
                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {

        return claimService.getClaimByReference(claimReference, authorisation)
            .orElseThrow(() -> new NotFoundException("Claim not found by claim reference " + claimReference));
    }

    @GetMapping("/representative/{externalReference}")
    @ApiOperation("Fetch user claims for given external reference number")
    public List<Claim> getClaimByExternalReference(
        @PathVariable("externalReference") String externalReference,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {

        return claimService.getClaimByExternalReference(externalReference, authorisation);
    }

    @GetMapping("/defendant/{defendantId}")
    @ApiOperation("Fetch claims linked to given defendant id")
    public List<Claim> getByDefendantId(
        @PathVariable("defendantId") String defendantId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.getClaimByDefendantId(defendantId, authorisation);
    }

    @PostMapping(value = "/{submitterId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("Creates a new claim")
    public Claim save(
        @Valid @NotNull @RequestBody ClaimData claimData,
        @PathVariable("submitterId") String submitterId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(value = "Features", required = false) List<String> features
    ) {
        return claimService.saveClaim(submitterId, claimData, authorisation, features);
    }

    @PostMapping(value = "/{submitterId}/create-legal-rep-claim", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("Creates a new legal rep claim")
    public Claim saveLegalRepresentedClaim(
        @Valid @NotNull @RequestBody ClaimData claimData,
        @PathVariable("submitterId") String submitterId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.saveRepresentedClaim(submitterId, claimData, authorisation);
    }

    @PostMapping(value = "/initiate-citizen-payment", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("Initiates a citizen payment")
    public CreatePaymentResponse initiatePayment(
        @Valid @NotNull @RequestBody ClaimData claimData,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.initiatePayment(authorisation, claimData);
    }

    @PutMapping(value = "/resume-citizen-payment", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("Resumes a citizen payment")
    public CreatePaymentResponse resumePayment(
        @Valid @NotNull @RequestBody ClaimData claimData,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.resumePayment(authorisation, claimData);
    }

    @PutMapping(value = "/create-citizen-claim", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("Creates a citizen claim")
    public Claim createClaim(
        @Valid @NotNull @RequestBody ClaimData claimData,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(value = "Features", required = false) List<String> features
    ) {
        return claimService.saveCitizenClaim(
            authorisation,
            claimData,
            features);
    }

    @PutMapping("/defendant/link")
    @ApiOperation("Links defendant to all unlinked letter-holder cases")
    public void linkDefendantToClaim(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        claimService.linkDefendantToClaim(authorisation);
    }

    @PostMapping(value = "/{externalId:" + UUID_PATTERN + "}/request-more-time")
    @ApiOperation("Updates response deadline. Can be called only once per each claim")
    public Claim requestMoreTimeToRespond(@PathVariable("externalId") String externalId,
                                          @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        return claimService.requestMoreTimeForResponse(externalId, authorisation);
    }

    @GetMapping("/{caseReference}/defendant-link-status")
    @ApiOperation("Check whether a claim is linked to a defendant")
    public DefendantLinkStatus isDefendantLinked(@PathVariable("caseReference") String caseReference) {
        boolean linked = claimService.getClaimByReferenceAnonymous(caseReference)
            .filter(claim -> claim.getDefendantId() != null)
            .isPresent();
        return new DefendantLinkStatus(linked);
    }

    @PutMapping(value = "/{externalId:" + UUID_PATTERN + "}/paid-in-full")
    public Claim paidInFull(
        @PathVariable("externalId") String externalId,
        @Valid @NotNull @RequestBody PaidInFull paidInFull,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation) {
        return claimService.paidInFull(externalId, paidInFull, authorisation);
    }

    @PutMapping(value = "/{externalId:" + UUID_PATTERN + "}/review-order")
    public Claim saveReviewOrder(
        @PathVariable("externalId") String externalId,
        @Valid @NotNull @RequestBody ReviewOrder reviewOrder,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation) {
        return claimService.saveReviewOrder(externalId, reviewOrder, authorisation);
    }
}
