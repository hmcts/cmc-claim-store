package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;

import java.net.URI;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.isSealedClaim;

@Component
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentUploader {

    private final DocumentManagementService documentManagementService;
    private final ClaimService claimService;

    @Autowired
    public DocumentUploader(DocumentManagementService documentManagementService,
                            ClaimService claimService) {
        this.documentManagementService = documentManagementService;
        this.claimService = claimService;
    }

    @EventListener
    public void uploadIntoDocumentManagementStore(DocumentGeneratedEvent event) {
        event.getDocuments().forEach(document -> {
            URI documentUri = this.documentManagementService.uploadDocument(
                event.getAuthorisation(),
                document.getFilename(),
                document.getBytes(),
                PDF.CONTENT_TYPE
            );

            if (isSealedClaim(document.getFilename())) {
                claimService.linkSealedClaimDocument(event.getAuthorisation(), event.getClaim(), documentUri);
            }
        });
    }
}
