package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import static java.util.Objects.requireNonNull;

@Component
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintHandler {

    private final PrintService bulkPrintService;

    @Autowired
    public BulkPrintHandler(PrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    @EventListener
    public void print(DocumentReadyToPrintEvent event) {
        requireNonNull(event);
        bulkPrintService.print(
            event.getClaim(),
            ImmutableMap.of(
                ClaimDocumentType.DEFENDANT_PIN_LETTER, event.getDefendantLetterDocument(),
                ClaimDocumentType.SEALED_CLAIM, event.getSealedClaimDocument())
        );
    }
}
