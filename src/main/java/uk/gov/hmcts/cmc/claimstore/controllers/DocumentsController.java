package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.controllers.advices.ResourceExceptionHandler;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api
@RestController
@RequestMapping("/documents")
public class DocumentsController {

    private final DocumentsService documentsService;

    private static final Logger logger = LoggerFactory.getLogger(ResourceExceptionHandler.class);

    @Autowired
    public DocumentsController(DocumentsService documentsService) {
        this.documentsService = documentsService;
    }

    @ApiOperation("Returns a Defendant Response copy for a given claim external id")
    @GetMapping(
        value = "/defendantResponseCopy/{externalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> defendantResponseCopy(
        @ApiParam("Claim external id")
        @PathVariable("externalId") @NotBlank String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        byte[] pdfDocument = documentsService.generateDefendantResponseReceipt(externalId, authorisation);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

    @ApiOperation("Returns a list of urls to all documents related to the given case")
    @PostMapping(
        value = "/all",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public void getAllDocumentsForCase(
        @ApiParam("Case details from ccd")
        @Valid @NotNull @RequestBody CaseDetails caseDetails,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        logger.info("I am here %s, %s", authorisation, caseDetails.toString());
    }

    @ApiOperation("Returns a sealed claim copy for a given claim external id")
    @GetMapping(
        value = "/legalSealedClaim/{externalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> legalSealedClaim(
        @ApiParam("Claim external id")
        @PathVariable("externalId") @NotBlank String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        byte[] pdfDocument = documentsService.getSealedClaim(externalId, authorisation);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

    @ApiOperation("Returns a County Court Judgement for a given claim external id")
    @GetMapping(
        value = "/ccj/{externalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> countyCourtJudgement(
        @ApiParam("Claim external id")
        @PathVariable("externalId") @NotBlank String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        byte[] pdfDocument = documentsService.generateCountyCourtJudgement(externalId, authorisation);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

    @ApiOperation("Returns a settlement agreement for a given claim external id")
    @GetMapping(
        value = "/settlementAgreement/{externalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> settlementAgreement(
        @ApiParam("Claim external id")
        @PathVariable("externalId") @NotBlank String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        byte[] pdfDocument = documentsService.generateSettlementAgreement(externalId, authorisation);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

    @ApiOperation("Returns a Defendant Response receipt for a given claim external id")
    @GetMapping(
        value = "/defendantResponseReceipt/{externalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> defendantResponseReceipt(
        @ApiParam("Claim external id")
        @PathVariable("externalId") @NotBlank String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        byte[] pdfDocument = documentsService.generateDefendantResponseReceipt(externalId, authorisation);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

    @ApiOperation("Returns a Claim Issue receipt for a given claim external id")
    @GetMapping(
        value = "/claimIssueReceipt/{externalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> claimIssueReceipt(
        @ApiParam("Claim external id")
        @PathVariable("externalId") @NotBlank String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        byte[] pdfDocument = documentsService.generateClaimIssueReceipt(externalId, authorisation);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }

    @ApiOperation("Returns a sealed claim copy for a given claim external id")
    @GetMapping(
        value = "/sealedClaim/{externalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> sealedClaim(
        @ApiParam("Claim external id")
        @PathVariable("externalId") @NotBlank String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        byte[] pdfDocument = documentsService.getSealedClaim(externalId, authorisation);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }
}
