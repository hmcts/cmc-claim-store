package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_eventOperations_enabled")
public class BulkPrintOperationService {

    private final BulkPrintService bulkPrintService;

    @Autowired
    public BulkPrintOperationService(BulkPrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    public Claim print(Claim claim, Document defendantLetterDocument, Document sealedClaimDocument) {
        //TODO check claim if operation already complete, if yes return claim else

        bulkPrintService.print(new DocumentReadyToPrintEvent(claim, defendantLetterDocument, sealedClaimDocument));

        //TODO update claim and return updated claim, below is placeholder
        return claim;
    }
}
