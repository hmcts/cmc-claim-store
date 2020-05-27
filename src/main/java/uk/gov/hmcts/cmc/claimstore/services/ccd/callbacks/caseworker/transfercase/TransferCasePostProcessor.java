package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.time.LocalDate;

@Service
public class TransferCasePostProcessor {

    private final CaseDetailsConverter caseDetailsConverter;
    private final TransferCaseNotificationsService transferCaseNotificationsService;
    private final TransferCaseLetterSender transferCaseLetterSender;
    private final TransferCaseDocumentService transferCaseDocumentService;

    public TransferCasePostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        TransferCaseNotificationsService transferCaseNotificationsService,
        TransferCaseLetterSender transferCaseLetterSender, TransferCaseDocumentService transferCaseDocumentService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.transferCaseNotificationsService = transferCaseNotificationsService;
        this.transferCaseLetterSender = transferCaseLetterSender;
        this.transferCaseDocumentService = transferCaseDocumentService;
    }

    public CallbackResponse completeCaseTransfer(CallbackParams callbackParams) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        ccdCase = attachNoticesOfTransferToCase(ccdCase);

        sendCaseDocumentsToBulkPrint(authorisation, ccdCase, claim);

        sendEmailNotifications(ccdCase, claim);

        ccdCase = updateCaseData(ccdCase);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private CCDCase attachNoticesOfTransferToCase(CCDCase ccdCase) {

        CCDCase ccdCaseWithDocuments = transferCaseDocumentService.attachNoticeOfTransferForCourt(ccdCase);

        if (!isDefendantLinked(ccdCase)) {

            ccdCaseWithDocuments = transferCaseDocumentService.attachNoticeOfTransferForDefendant(ccdCaseWithDocuments);
        }

        return ccdCaseWithDocuments;
    }

    private CCDCase updateCaseData(CCDCase ccdCase) {

        return ccdCase.toBuilder()
            .coverLetterDoc(null)
            .transferContent(ccdCase.getTransferContent().toBuilder().dateOfTransfer(LocalDate.now()).build())
            .build();
    }

    private void sendEmailNotifications(CCDCase ccdCase, Claim claim) {

        transferCaseNotificationsService.sendClaimUpdatedEmailToClaimant(claim);

        if (isDefendantLinked(ccdCase)) {
            transferCaseNotificationsService.sendClaimUpdatedEmailToDefendant(claim);
        }
    }

    private void sendCaseDocumentsToBulkPrint(String authorisation, CCDCase ccdCase, Claim claim) {

        if (!isDefendantLinked(ccdCase)) {
            transferCaseLetterSender.sendNoticeOfTransferForDefendant(authorisation, ccdCase, claim);
        }

        transferCaseLetterSender.sendAllCaseDocumentsToCourt(authorisation, ccdCase, claim);
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }
}
