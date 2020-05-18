package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;

@Service
public class BulkPrintTransferService {

    private final GeneralLetterService generalLetterService;
    private final String noticeOfTransferLetterTemplateId;
    private final String noticeTransferLetterTemplateId;

    public BulkPrintTransferService(
        GeneralLetterService generalLetterService,
        @Value("${doc_assembly.noticeOfTransferLetterTemplateId}") String noticeOfTransferLetterTemplateId,
        @Value("${doc_assembly.noticeTransferLetterTemplateId}") String noticeTransferLetterTemplateId
    ) {
        this.generalLetterService = generalLetterService;
        this.noticeOfTransferLetterTemplateId = noticeOfTransferLetterTemplateId;
        this.noticeTransferLetterTemplateId = noticeTransferLetterTemplateId;
    }


    public String generateNoticeOfTransferLetter(CCDCase ccdCase) {
        // TODO
        return null;
    }

    public void addNoticeOfTransferToCaseRecordToCaseDocuments(CCDCase ccdCase, String noticeOfTransferLetter) {
        // TODO
    }

    public String generateTransferNoticeLetter(CCDCase ccdCase) {
        // TODO
        return null;
    }

    public void addTransferLetterNoticeToCaseDocuments(CCDCase ccdCase, String transferNoticeLetter) {
        // TODO
    }

    public void sendCaseDocumentsToBulkPrint(CCDCase ccdCase) {
        // TODO
    }

    public void sendClaimUpdatedEmailToClaimant(CCDCase ccdCase) {
        // TODO
    }

    public void sendClaimUpdatedEmailToDefendant(CCDCase ccdCase) {
        // TODO
    }
}
