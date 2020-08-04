package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentSubtype;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentType;

import javax.validation.constraints.NotBlank;

@Api
@RestController
@RequestMapping("/scanned-documents")
public class ScannedDocumentsController {

    private final DocumentsService documentsService;

    @Autowired
    public ScannedDocumentsController(DocumentsService documentsService) {

        this.documentsService = documentsService;
    }

    @ApiOperation("Returns a scanned pdf for a given claim external id")
    @GetMapping(
        value = "/{externalId}/{documentType}/{documentSubtype}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> scannedDocument(
        @ApiParam("Claim external id")
        @PathVariable("externalId") @NotBlank String externalId,
        @ApiParam("Claim document type")
        @PathVariable("documentType") @NotBlank String documentType,
        @ApiParam("Claim document subtype")
        @PathVariable("documentSubtype") @NotBlank String documentSubtype,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        ScannedDocumentType scannedDocumentType = ScannedDocumentType.fromValue(documentType);
        ScannedDocumentSubtype scannedDocumentSubtype = ScannedDocumentSubtype.valueOf(documentSubtype.toUpperCase());

        byte[] pdfDocument = documentsService.generateScannedDocument(externalId, scannedDocumentType,
            scannedDocumentSubtype, authorisation);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }
}
