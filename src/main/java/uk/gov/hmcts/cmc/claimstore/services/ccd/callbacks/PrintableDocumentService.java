package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import org.springframework.beans.factory.annotation.Autowired;
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

    private final DocumentManagementService documentManagementService;

    @Autowired
    public PrintableDocumentService(DocumentManagementService documentManagementService) {
        this.documentManagementService = documentManagementService;
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
}
