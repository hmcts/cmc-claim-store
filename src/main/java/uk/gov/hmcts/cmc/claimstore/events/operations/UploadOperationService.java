package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentUploadHandler;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static java.util.Collections.singletonList;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled")
public class UploadOperationService {

    private final DocumentUploadHandler documentUploadHandler;

    @Autowired
    public UploadOperationService(DocumentUploadHandler documentUploadHandler) {
        this.documentUploadHandler = documentUploadHandler;
    }

    public Claim uploadDocument(Claim claim, String authorisation, PDF document) {
        //TODO check claim if operation already complete, if yes return claim else

        return documentUploadHandler.uploadToDocumentManagement(claim, authorisation, singletonList(document));
    }
}
