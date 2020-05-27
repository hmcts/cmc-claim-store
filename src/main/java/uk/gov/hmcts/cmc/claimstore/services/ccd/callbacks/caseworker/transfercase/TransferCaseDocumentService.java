package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_COURT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_DEFENDANT;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildNoticeOfTransferForCourtFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildNoticeOfTransferForDefendantFileBaseName;

@Service
public class TransferCaseDocumentService {

    private final GeneralLetterService generalLetterService;

    public TransferCaseDocumentService(GeneralLetterService generalLetterService) {
        this.generalLetterService = generalLetterService;
    }

    public CCDCase attachNoticeOfTransferForCourt(CCDCase ccdCase) {

        return generalLetterService.attachGeneralLetterToCase(ccdCase, ccdCase.getCoverLetterDoc(),
            buildNoticeOfTransferLetterFileName(ccdCase, FOR_COURT));
    }

    public CCDCase attachNoticeOfTransferForDefendant(CCDCase ccdCase) {

        return generalLetterService.attachGeneralLetterToCase(ccdCase,
            ccdCase.getDraftLetterDoc(),
            buildNoticeOfTransferLetterFileName(ccdCase, FOR_DEFENDANT));
    }

    private String buildNoticeOfTransferLetterFileName(
        CCDCase ccdCase,
        NoticeOfTransferLetterType noticeOfTransferLetterType
    ) {
        String basename;

        switch (noticeOfTransferLetterType) {
            case FOR_COURT:
                basename = buildNoticeOfTransferForCourtFileBaseName(ccdCase.getPreviousServiceCaseReference());
                break;
            case FOR_DEFENDANT:
                basename = buildNoticeOfTransferForDefendantFileBaseName(ccdCase.getPreviousServiceCaseReference());
                break;
            default:
                throw new IllegalArgumentException();
        }

        return String.format("%s.pdf", basename);
    }
}
