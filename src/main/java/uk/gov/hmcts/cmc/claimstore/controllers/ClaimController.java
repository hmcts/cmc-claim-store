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
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.DefendantLinkStatus;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api
@RestController
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ClaimController {

    public static final String UUID_PATTERN = "\\p{XDigit}{8}-\\p{XDigit}"
        + "{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}";

    public static final String CLAIM_REFERENCE_PATTERN = "^\\d{3}(?:LR|MC)\\d{3}$";

    private final ClaimService claimService;

    @Autowired
    public ClaimController(final ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping("/claimant/{submitterId:\\d+}")
    @ApiOperation("Fetch user claims for given submitter id")
    public List<Claim> getBySubmitterId(@PathVariable("submitterId") final Long submitterId) {
        return claimService.getClaimBySubmitterId(submitterId);
    }

    @GetMapping("/letter/{letterHolderId:\\d+}")
    @ApiOperation("Fetch user claim for given letter holder id")
    public Claim getByLetterHolderId(@PathVariable("letterHolderId") final Long letterHolderId) {
        return claimService.getClaimByLetterHolderId(letterHolderId);
    }

    @GetMapping("/{externalId:" + UUID_PATTERN + "}")
    @ApiOperation("Fetch claim for given external id")
    public Claim getByExternalId(@PathVariable("externalId") final String externalId) {
        return claimService.getClaimByExternalId(externalId);
    }

    @GetMapping("/{claimReference:" + CLAIM_REFERENCE_PATTERN + "}")
    @ApiOperation("Fetch claim for given claim reference")
    public Claim getByClaimReference(@PathVariable("claimReference") final String claimReference) {
        return claimService.getClaimByReference(claimReference)
            .orElseThrow(() -> new NotFoundException("Claim not found by claim reference " + claimReference));
    }

    @GetMapping("/representative/{externalReference}")
    @ApiOperation("Fetch user claims for given external reference number")
    public List<Claim> getClaimByExternalReference(@PathVariable("externalReference") final String externalReference) {
        return claimService.getClaimByExternalReference(externalReference);
    }

    @GetMapping("/defendant/{defendantId:\\d+}")
    @ApiOperation("Fetch claims linked to given defendant id")
    public List<Claim> getByDefendantId(@PathVariable("defendantId") final Long defendantId) {
        return claimService.getClaimByDefendantId(defendantId);
    }

    @PostMapping(value = "/{submitterId:\\d+}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("Creates a new claim")
    public Claim save(@Valid @NotNull @RequestBody final ClaimData claimData,
                      @PathVariable("submitterId") final Long submitterId,
                      @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation) {
        return claimService.saveClaim(submitterId, claimData, authorisation);
    }

    @PutMapping("/{claimId:\\d+}/defendant/{defendantId:\\d+}")
    @ApiOperation("Links defendant to existing claim")
    public Claim linkDefendantToClaim(@PathVariable("claimId") final Long claimId,
                                      @PathVariable("defendantId") final Long defendantId) {
        claimService.linkDefendantToClaim(claimId, defendantId);
        return claimService.getClaimById(claimId);
    }

    @PostMapping(value = "/{claimId:\\d+}/request-more-time")
    @ApiOperation("Updates response deadline. Can be called only once per each claim")
    public Claim requestMoreTimeToRespond(@PathVariable("claimId") final Long claimId,
                                          @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation) {
        return claimService.requestMoreTimeForResponse(claimId, authorisation);
    }

    @GetMapping("/{caseReference}/defendant-link-status")
    @ApiOperation("Check whether a claim is linked to a defendant")
    public DefendantLinkStatus isDefendantLinked(@PathVariable("caseReference") final String caseReference) {
        Boolean linked = claimService.getClaimByReference(caseReference)
            .filter(claim -> claim.getDefendantId() != null)
            .isPresent();
        return new DefendantLinkStatus(linked);
    }
}
