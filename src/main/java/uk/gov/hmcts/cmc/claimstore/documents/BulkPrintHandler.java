package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;

import java.util.Arrays;

@Service
public class BulkPrintHandler {

    private final BulkPrintService bulkPrintService;

    @Autowired
    public BulkPrintHandler(BulkPrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    @EventListener
    public void print(DocumentReadyToPrintEvent event) {
        bulkPrintService
            .print(event.getClaim(), Arrays.asList(event.getDefendantLetterDocument(), event.getSealedClaimDocument()));
    }
}
