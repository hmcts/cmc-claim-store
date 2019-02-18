package uk.gov.hmcts.cmc.claimstore.documents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;

@Component
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentUploader {
    private static final Logger logger = LoggerFactory.getLogger(DocumentUploader.class);
    private final DocumentsService documentService;

    public DocumentUploader(DocumentsService documentService) {
        this.documentService = documentService;
    }

    @EventListener
    public void uploadIntoDocumentManagementStore(DocumentGeneratedEvent event) {
        event.getDocuments().forEach(document -> {
            try {
                documentService.uploadToDocumentManagement(document,
                    event.getAuthorisation(),
                    event.getClaim());
            } catch (Exception ex) {
                logger.warn(String.format("unable to upload document %s into document management",
                    document.getFilename()), ex);
            }
        });
    }

}
