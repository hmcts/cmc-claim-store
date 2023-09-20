package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintHandler;


@Service
public class ClaimantRejectionDefendantNotificationService {

    private final BulkPrintHandler bulkPrintHandler;

    @Autowired
    public ClaimantRejectionDefendantNotificationService(BulkPrintHandler bulkPrintHandler){
        this.bulkPrintHandler = bulkPrintHandler;
    }

}
