package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.isSealedClaim;

@Component
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class DocumentPrinter {

    private final BulkPrintService bulkPrintService;

    public DocumentPrinter(BulkPrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    @EventListener
    public void uploadIntoDocumentManagementStore(DocumentGeneratedEvent event) {
        event.
    }
}
