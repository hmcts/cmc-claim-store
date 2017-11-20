package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.DocumentsService;

@Api
@RestController
@RequestMapping("/documents")
public class DocumentsController {

    private final DocumentsService documentsService;

    @Autowired
    public DocumentsController(final DocumentsService documentsService) {
        this.documentsService = documentsService;
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
        final byte[] pdfDocument = documentsService.generateDefendantResponseCopy(claimExternalId);

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
    ) {
        final byte[] pdfDocument = documentsService.generateLegalSealedClaim(claimExternalId, authorisation);

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
        final byte[] pdfDocument = documentsService.generateCountyCourtJudgement(claimExternalId);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

}
