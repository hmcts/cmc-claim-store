package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

@Service
public class TransferCasePostProcessor {

    private final CaseDetailsConverter caseDetailsConverter;
    private final GeneralLetterService generalLetterService;

    public TransferCasePostProcessor(
        CaseDetailsConverter caseDetailsConverter, GeneralLetterService generalLetterService) {

        this.caseDetailsConverter = caseDetailsConverter;
        this.generalLetterService = generalLetterService;
    }

    public CallbackResponse performBulkPrintTransfer(CallbackParams callbackParams) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        ccdCase = attachNoticeOfTransferLetterToCase(ccdCase, authorisation, NoticeOfTransferLetterType.FOR_COURT);

        if (!isDefendantLinked(ccdCase)) {
            ccdCase = attachNoticeOfTransferLetterToCase(ccdCase, authorisation, NoticeOfTransferLetterType.FOR_DEFENDANT);
        }

        sendCaseDocumentsToBulkPrint(ccdCase);

        sendClaimUpdatedEmailToClaimant(ccdCase);

        if (isDefendantLinked(ccdCase)) {
            sendClaimUpdatedEmailToDefendant(ccdCase);
        }

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private CCDCase attachNoticeOfTransferLetterToCase(CCDCase ccdCase, String authorisation,
                                                      NoticeOfTransferLetterType noticeOfTransferLetterType) {

        CCDDocument noticeOfTransferLetter = ccdCase.getDraftLetterDoc();   // TODO depends on notice type

        return generalLetterService.attachGeneralLetterToCase(ccdCase, noticeOfTransferLetter,
            noticeOfTransferLetterType.documentName);
    }

    private void sendCaseDocumentsToBulkPrint(CCDCase ccdCase) {
        // TODO Based on GeneralLetterService.printLetter
    }

    private void sendClaimUpdatedEmailToClaimant(CCDCase ccdCase) {
        // TODOn Based on ChangeContactDetailsNotificationService
    }

    private void sendClaimUpdatedEmailToDefendant(CCDCase ccdCase) {
        // TODO Based on ChangeContactDetailsNotificationService
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }
}
