package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;

@Service
public class TransferCaseService {

    private final GeneralLetterService generalLetterService;
    private final String noticeOfTransferSentToCourtTemplateId;
    private final String noticeOfTransferSentToDefendantTemplateId;

    public TransferCaseService(
        GeneralLetterService generalLetterService,
        @Value("${doc_assembly.noticeOfTransferSentToCourtTemplateId}") String noticeOfTransferSentToCourtTemplateId,
        @Value("${doc_assembly.noticeOfTransferSentToDefendantTemplateId}")
            String noticeOfTransferSentToDefendantTemplateId
    ) {
        this.generalLetterService = generalLetterService;
        this.noticeOfTransferSentToCourtTemplateId = noticeOfTransferSentToCourtTemplateId;
        this.noticeOfTransferSentToDefendantTemplateId = noticeOfTransferSentToDefendantTemplateId;
    }

    public CCDCase addNoticeOfTransferLetterToCaseDocuments(CCDCase ccdCase, String authorisation,
                                                            NoticeOfTransferLetter noticeOfTransferLetter) {

        String letterUrl = generalLetterService.generateLetter(ccdCase, authorisation,
            templateId(noticeOfTransferLetter));

        CCDDocument transferNoticeLetter = CCDDocument.builder().documentUrl(letterUrl).build();

        return generalLetterService.addCaseDocument(ccdCase, transferNoticeLetter,
            noticeOfTransferLetter.documentName);
    }

    public void sendCaseDocumentsToBulkPrint(CCDCase ccdCase) {
        // TODO Based on GeneralLetterService.printLetter
    }

    public void sendClaimUpdatedEmailToClaimant(CCDCase ccdCase) {
        // TODO
    }

    public void sendClaimUpdatedEmailToDefendant(CCDCase ccdCase) {
        // TODO
    }

    public enum NoticeOfTransferLetter {
        FOR_COURT("Notice of Transfer sent to court"),
        FOR_DEFENDANT("Notice of Transfer sent to defendant");

        public final String documentName;

        private NoticeOfTransferLetter(String documentName) {
            this.documentName = documentName;
        }
    }

    private String templateId(NoticeOfTransferLetter noticeOfTransferLetter) {
        switch (noticeOfTransferLetter) {
            case FOR_COURT:
                return noticeOfTransferSentToCourtTemplateId;
            case FOR_DEFENDANT:
                return noticeOfTransferSentToDefendantTemplateId;
            default:
                throw new IllegalArgumentException(noticeOfTransferLetter.name());
        }
    }
}
