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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import javax.validation.constraints.NotBlank;

@Api
@RestController
@RequestMapping("/documents")
public class DocumentsController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DocumentsService documentsService;

    @Autowired
    public DocumentsController(DocumentsService documentsService) {
        this.documentsService = documentsService;
    }

    @ApiOperation("Returns a specific pdf for a given claim external id")
    @GetMapping(
        value = "/{documentType}/{externalId}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> document(
        @ApiParam("Claim document type")
        @PathVariable("documentType") @NotBlank String documentType,
        @ApiParam("Claim external id")
        @PathVariable("externalId") @NotBlank String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        ClaimDocumentType claimDocumentType = ClaimDocumentType.fromValue(documentType);

        logger.info("Received request to create/download pdf of type " + claimDocumentType.name());

        byte[] pdfDocument = documentsService.generateDocument(externalId, claimDocumentType, authorisation);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }
}
