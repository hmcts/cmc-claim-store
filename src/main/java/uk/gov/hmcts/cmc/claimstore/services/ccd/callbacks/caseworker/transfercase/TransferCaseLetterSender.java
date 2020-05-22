package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;

@Component
public class TransferCaseLetterSender {
    public void sendNoticeOfTransferForDefendant(CCDCase ccdCase) {
        // TODO Based on GeneralLetterService.printLetter
    }

    public void sendAllCaseDocumentsToCourt(CCDCase ccdCase) {
        // TODO
    }
}
