package uk.gov.hmcts.cmc.claimstore.controllers.support;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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

@Tag(name = "Case Metadata Controller")
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
    @Operation(summary = "Fetch user case metadata for given submitter id")
    public List<CaseMetadata> getBySubmitterId(
        @PathVariable("submitterId") String submitterId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.getClaimBySubmitterId(
            submitterId,
            authorisation, 0)
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }

    @GetMapping("/defendant/{defendantId}/metadata")
    @Operation(summary = "Fetch case metadata for given defendant id")
    public List<CaseMetadata> getByDefendantId(
        @PathVariable("defendantId") String defendantId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.getClaimByDefendantId(
            defendantId,
            authorisation, 0)
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }

    @GetMapping("/{externalId:" + UUID_PATTERN + "}/metadata")
    @Operation(summary = "Fetch case metadata for given external id")
    public CaseMetadata getByExternalId(
        @PathVariable("externalId") String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return fromClaim(claimService.getClaimByExternalId(
            externalId,
            authorisation
            )
        );
    }

    @GetMapping("/{claimReference:" + CLAIM_REFERENCE_PATTERN + "}/metadata")
    @Operation(summary = "Fetch claim metadata for given claim reference")
    public CaseMetadata getByClaimReference(@PathVariable("claimReference") String claimReference) {
        return claimService.getClaimByReferenceAnonymous(
            claimReference
        )
            .map(CaseMetadata::fromClaim)
            .orElseThrow(() -> new NotFoundException("Claim not found by claim reference " + claimReference));
    }

    @PostMapping("/filters/claimants/email")
    public List<CaseMetadata> getByClaimantEmailFilter(
        @RequestParam("email") String email,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.getClaimByClaimantEmail(
            email,
            authorisation
        )
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }

    @PostMapping("/filters/defendants/email")
    public List<CaseMetadata> getByDefendantEmailFilter(
        @RequestParam("email") String email,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.getClaimByDefendantEmail(
            email,
            authorisation
        )
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }

    @PostMapping("/filters/payments")
    public List<CaseMetadata> getByPaymentReference(
        @RequestParam("reference") String payReference,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.getClaimByPaymentReference(
            payReference,
            authorisation
        )
            .stream()
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }

    @GetMapping("/filters/created")
    public List<CaseMetadata> getCreatedCases(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        return claimService.getClaimsByState(CREATE, userService.getUser(authorisation))
            .stream()
            .filter(claim -> claim.getReferenceNumber() != null)
            .map(CaseMetadata::fromClaim)
            .collect(Collectors.toList());
    }
}
