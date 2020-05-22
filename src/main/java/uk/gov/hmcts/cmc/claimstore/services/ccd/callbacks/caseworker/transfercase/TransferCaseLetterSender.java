package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;


import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableDocument;
import uk.gov.hmcts.cmc.claimstore.events.BulkPrintTransferEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.List;

@Component
public class TransferCaseLetterSender {

    private EventProducer eventProducer;

    public TransferCaseLetterSender(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    public void sendNoticeOfTransferForDefendant(CCDCase ccdCase) {
        // TODO Based on GeneralLetterService.printLetter
    }

}
