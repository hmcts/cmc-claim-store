package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.domain.models.Claim;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_COURT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_DEFENDANT;

@Service
public class TransferCaseDocumentPublishService {

    private final TransferCaseLetterSender transferCaseLetterSender;
    private final TransferCaseDocumentService transferCaseDocumentService;
    private final String courtLetterTemplateId;
    private final String defendantLetterTemplateId;
    private final CoverLetterGenerator coverLetterGenerator;

    public TransferCaseDocumentPublishService(
        TransferCaseLetterSender transferCaseLetterSender,
        TransferCaseDocumentService transferCaseDocumentService,
        @Value("${doc_assembly.noticeOfTransferSentToCourtTemplateId}") String courtLetterTemplateId,
        @Value("${doc_assembly.noticeOfTransferSentToDefendantTemplateId}") String defendantLetterTemplateId,
        CoverLetterGenerator coverLetterGenerator

    ) {
        this.transferCaseLetterSender = transferCaseLetterSender;
        this.transferCaseDocumentService = transferCaseDocumentService;
        this.courtLetterTemplateId = courtLetterTemplateId;
        this.defendantLetterTemplateId = defendantLetterTemplateId;
        this.coverLetterGenerator = coverLetterGenerator;
    }

    public CCDCase publishCaseDocuments(CCDCase ccdCase, String authorisation, Claim claim) {

        CCDCase updated = publishLetterToDefendant(ccdCase, authorisation, claim);

        return publishCaseDocumentsToCourt(updated, authorisation, claim);
    }

    private CCDCase publishLetterToDefendant(CCDCase ccdCase, String authorisation, Claim claim) {
        if (isDefendantLinked(ccdCase)) {
            return ccdCase;
        }

        CCDDocument defendantLetter = coverLetterGenerator
            .generate(ccdCase, authorisation, FOR_DEFENDANT, defendantLetterTemplateId);

        transferCaseLetterSender.sendNoticeOfTransferForDefendant(authorisation, defendantLetter, claim);

        return transferCaseDocumentService.attachNoticeOfTransfer(ccdCase, defendantLetter, authorisation);
    }

    private CCDCase publishCaseDocumentsToCourt(CCDCase ccdCase, String authorisation, Claim claim) {

        CCDDocument coverDoc = coverLetterGenerator
            .generate(ccdCase, authorisation, FOR_COURT, courtLetterTemplateId);
        transferCaseLetterSender.sendAllCaseDocumentsToCourt(authorisation, ccdCase, claim, coverDoc);

        return transferCaseDocumentService.attachNoticeOfTransfer(ccdCase, coverDoc, authorisation);
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }
}
