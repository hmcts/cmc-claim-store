package uk.gov.hmcts.cmc.claimstore.controllers.support;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.metadata.CaseMetadata;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.CLAIM_REFERENCE_PATTERN;
import static uk.gov.hmcts.cmc.claimstore.controllers.PathPatterns.UUID_PATTERN;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.CREATE;
import static uk.gov.hmcts.cmc.domain.models.metadata.CaseMetadata.fromClaim;

@Api
@RestController
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class CaseMetadataController {

    private final ClaimService claimService;
    private final UserService userService;

    @Autowired
    public CaseMetadataController(
        ClaimService claimService,
        UserService userService
    ) {
        this.claimService = claimService;
        this.userService = userService;

    }

    @GetMapping("/claimant/{submitterId}/metadata")
    @ApiOperation("Fetch user case metadata for given submitter id")
    public List<CaseMetadata> getBySubmitterId(@PathVariable("submitterId") String submitterId) {
        return claimService.getClaimBySubmitterId(
            submitterId,
            userService.authenticateAnonymousCaseWorker().getAuthorisation()
        )
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }

    @GetMapping("/defendant/{defendantId}/metadata")
    @ApiOperation("Fetch case metadata for given defendant id")
    public List<CaseMetadata> getByDefendantId(
        @PathVariable("defendantId") String defendantId
    ) {
        return claimService.getClaimByDefendantId(
            defendantId,
            userService.authenticateAnonymousCaseWorker().getAuthorisation()
        )
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }

    @GetMapping("/{externalId:" + UUID_PATTERN + "}/metadata")
    @ApiOperation("Fetch case metadata for given external id")
    public CaseMetadata getByExternalId(@PathVariable("externalId") String externalId) {
        return fromClaim(claimService.getClaimByExternalId(
            externalId,
            userService.authenticateAnonymousCaseWorker()
            )
        );
    }

    @GetMapping("/{claimReference:" + CLAIM_REFERENCE_PATTERN + "}/metadata")
    @ApiOperation("Fetch claim metadata for given claim reference")
    public CaseMetadata getByClaimReference(@PathVariable("claimReference") String claimReference) {
        return claimService.getClaimByReferenceAnonymous(
            claimReference
        )
            .map(CaseMetadata::fromClaim)
            .orElseThrow(() -> new NotFoundException("Claim not found by claim reference " + claimReference));
    }

    @PostMapping("/filters/claimants/email")
    public List<CaseMetadata> getByClaimantEmailFilter(@RequestParam("email") String email) {
        return claimService.getClaimByClaimantEmail(
            email,
            userService.authenticateAnonymousCaseWorker().getAuthorisation()
        )
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }

    @PostMapping("/filters/defendants/email")
    public List<CaseMetadata> getByDefendantEmailFilter(@RequestParam("email") String email) {
        return claimService.getClaimByDefendantEmail(
            email,
            userService.authenticateAnonymousCaseWorker().getAuthorisation()
        )
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }

    @PostMapping("/filters/payments")
    public List<CaseMetadata> getByPaymentReference(@RequestParam("reference") String payReference) {
        return claimService.getClaimByPaymentReference(
            payReference,
            userService.authenticateAnonymousCaseWorker().getAuthorisation()
        )
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }

    @GetMapping("/filters/created")
    public List<CaseMetadata> getCreatedCases() {
        return claimService.getClaimsByState(CREATE, userService.authenticateAnonymousCaseWorker())
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }
}
