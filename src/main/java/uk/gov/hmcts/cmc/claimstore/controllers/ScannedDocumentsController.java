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

import javax.validation.constraints.NotBlank;

@Api
@RestController
@RequestMapping("/scanned-documents")
public class ScannedDocumentsController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DocumentsService documentsService;

    @Autowired
    public ScannedDocumentsController(DocumentsService documentsService) {

        this.documentsService = documentsService;
    }

    @ApiOperation("Returns the OCON9x scanned form for the given claim")
    @GetMapping(
        value = "/{externalId}/OCON9X",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> ocon9xForm(
        @ApiParam("Claim external id")
        @PathVariable("externalId") @NotBlank String externalId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {

        logger.info("Received request to download OCON9x form for {}", externalId);

        byte[] pdfDocument = documentsService.getOCON9xForm(externalId, authorisation);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }
}
