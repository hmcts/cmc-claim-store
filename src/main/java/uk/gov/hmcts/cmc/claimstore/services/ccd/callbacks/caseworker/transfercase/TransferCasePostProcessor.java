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
    private final TransferCaseLetterSender transferCaseLetterSender;

    public TransferCasePostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        GeneralLetterService generalLetterService,
        TransferCaseLetterSender transferCaseLetterSender) {

        this.caseDetailsConverter = caseDetailsConverter;
        this.generalLetterService = generalLetterService;
        this.transferCaseLetterSender = transferCaseLetterSender;
    }

    public CallbackResponse performBulkPrintTransfer(CallbackParams callbackParams) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        ccdCase = attachNoticesOfTransferToCase(ccdCase);

        sendCaseDocumentsToBulkPrint(ccdCase);

        sendEmailNotifications(ccdCase);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private CCDCase attachNoticesOfTransferToCase(CCDCase ccdCase) {

        ccdCase = generalLetterService.attachGeneralLetterToCase(ccdCase, ccdCase.getCoverLetterDoc(),
            "Notice of transfer letter for court");

        if (!isDefendantLinked(ccdCase)) {

            ccdCase = generalLetterService.attachGeneralLetterToCase(ccdCase, ccdCase.getDraftLetterDoc(),
                "Notice of transfer letter for defendant");
        }

        return ccdCase;
    }

    private void sendCaseDocumentsToBulkPrint(CCDCase ccdCase) {

        if (!isDefendantLinked(ccdCase)) {
            transferCaseLetterSender.sendNoticeOfTransferForDefendant(ccdCase);
        }

        transferCaseLetterSender.sendAllCaseDocumentsToCourt(ccdCase);
    }

    private void sendEmailNotifications(CCDCase ccdCase) {
        sendClaimUpdatedEmailToClaimant(ccdCase);

        if (isDefendantLinked(ccdCase)) {
            sendClaimUpdatedEmailToDefendant(ccdCase);
        }
    }

    private void sendClaimUpdatedEmailToClaimant(CCDCase ccdCase) {
        // TODO Based on ChangeContactDetailsNotificationService
    }

    private void sendClaimUpdatedEmailToDefendant(CCDCase ccdCase) {
        // TODO Based on ChangeContactDetailsNotificationService
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }
}
