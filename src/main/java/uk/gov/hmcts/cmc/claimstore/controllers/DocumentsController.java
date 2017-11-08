package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.CountyCourtJudgmentService;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;

@Api
@RestController
@RequestMapping("/documents")
public class DocumentsController {

    private final ClaimService claimService;
    private final DefendantResponseCopyService defendantResponseCopyService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final CountyCourtJudgmentService countyCourtJudgmentService;
    private final DocumentManagementService documentManagementService;
    private final boolean dmFeatureToggle;

    @Autowired
    public DocumentsController(
        final ClaimService claimService,
        final DefendantResponseCopyService defendantResponseCopyService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final CountyCourtJudgmentService countyCourtJudgmentService,
        final DocumentManagementService documentManagementService,
        @Value("${feature_toggles.document_management}") final boolean dmFeatureToggle
    ) {
        this.claimService = claimService;
        this.defendantResponseCopyService = defendantResponseCopyService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.countyCourtJudgmentService = countyCourtJudgmentService;
        this.documentManagementService = documentManagementService;
        this.dmFeatureToggle = dmFeatureToggle;
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
        @PathVariable("claimExternalId") @NotBlank String claimExternalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation
    ) throws DocumentManagementException {
        Claim claim = claimService.getClaimByExternalId(claimExternalId);
        byte[] pdfDocument = getPdfDocument(claim, authorisation);
        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

    private byte[] getPdfDocument(final Claim claim, final String authorisation) throws DocumentManagementException {
        final byte[] n1ClaimPdf = legalSealedClaimPdfService.createPdf(claim);
        if (dmFeatureToggle) {
            return n1ClaimPdf;
        } else {
            return documentManagementService.getClaimN1Form(authorisation, claim, n1ClaimPdf);
        }
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
