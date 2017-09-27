package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimService;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.CountyCourtJudgmentService;
import uk.gov.hmcts.cmc.claimstore.services.DefendantResponseService;

@Api
@RestController
@RequestMapping("/documents")
public class DocumentsController {

    private final ClaimService claimService;
    private final DefendantResponseCopyService defendantResponseCopyService;
    private final LegalSealedClaimService legalSealedClaimService;
    private final CountyCourtJudgmentService countyCourtJudgmentService;

    public DocumentsController(
        final ClaimService claimService,
        final DefendantResponseCopyService defendantResponseCopyService,
        final LegalSealedClaimService legalSealedClaimService,
        final CountyCourtJudgmentService countyCourtJudgmentService
    ) {
        this.claimService = claimService;
        this.defendantResponseCopyService = defendantResponseCopyService;
        this.legalSealedClaimService = legalSealedClaimService;
        this.countyCourtJudgmentService = countyCourtJudgmentService;
    }

    @ApiOperation("Returns a Defendant Response copy for a given claim external id")
    @GetMapping(
        value = "/defendantResponseCopy/{claimExternalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> defendantResponseCopy(
        @ApiParam("Claim external id")
        @PathVariable("claimExternalId") @NotBlank String claimExternalId
    ) {
        Claim claim = claimService.getClaimByExternalId(claimExternalId);
        byte[] pdfDocument = defendantResponseCopyService.createPdf(claim);
        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

    @ApiOperation("Returns a sealed claim copy for a given claim external id")
    @GetMapping(
        value = "/legalSealedClaim/{claimExternalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> legalSealedClaim(
        @ApiParam("Claim external id")
        @PathVariable("claimExternalId") @NotBlank String claimExternalId
    ) {
        Claim claim = claimService.getClaimByExternalId(claimExternalId);
        byte[] pdfDocument = legalSealedClaimService.createPdf(claim);
        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

    @ApiOperation("Returns a County Court Judgement for a given claim external id")
    @GetMapping(
        value = "/ccj/{claimExternalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> countyCourtJudgement(
        @ApiParam("Claim external id")
        @PathVariable("claimExternalId") @NotBlank String claimExternalId
    ) {
        Claim claim = claimService.getClaimByExternalId(claimExternalId);
        byte[] pdfDocument = countyCourtJudgmentService.createPdf(claim);
        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

}
