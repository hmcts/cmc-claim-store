package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Collections;

@Service
public class PrintableDocumentService {

    private final boolean secureDocumentManagement;
    private final DocumentManagementService documentManagementService;
    private final DocumentManagementService secureDocumentManagementService;

    @Autowired
    public PrintableDocumentService(
        @Value("${document_management.secured}")
            boolean secureDocumentManagement,
        @Qualifier("legacyDocumentManagementService")
            DocumentManagementService documentManagementService,
        @Qualifier("securedDocumentManagementService")
            DocumentManagementService secureDocumentManagementService) {
        this.secureDocumentManagement = secureDocumentManagement;
        this.documentManagementService = documentManagementService;
        this.secureDocumentManagementService = secureDocumentManagementService;
    }

    public Document process(CCDDocument document, String authorisation) {
        try {
            return new Document(Base64.getEncoder().encodeToString(
                documentManagementService.downloadDocument(
                    authorisation,
                    ClaimDocument.builder()
                        .documentName(document.getDocumentFileName())
                        .documentManagementUrl(new URI(document.getDocumentUrl()))
                        .build())),
                Collections.emptyMap());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public byte[] pdf(CCDDocument document, String authorisation) {
        try {
            return !secureDocumentManagement ? documentManagementService.downloadDocument(
                authorisation,
                ClaimDocument.builder()
                    .documentName(document.getDocumentFileName())
                    .documentManagementUrl(new URI(document.getDocumentUrl()))
                    .build()) :
                secureDocumentManagementService.downloadDocument(
                    authorisation,
                    ClaimDocument.builder()
                        .documentName(document.getDocumentFileName())
                        .documentManagementUrl(new URI(document.getDocumentUrl()))
                        .build());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
