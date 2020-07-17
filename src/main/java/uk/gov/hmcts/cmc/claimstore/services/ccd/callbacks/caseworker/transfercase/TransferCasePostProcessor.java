package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.elasticsearch.common.TriFunction;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

@Service
public class TransferCasePostProcessor {

    private final CaseDetailsConverter caseDetailsConverter;
    private final TransferCaseNotificationsService transferCaseNotificationsService;
    private final TransferCaseDocumentPublishService transferCaseDocumentPublishService;
    private final BulkPrintTransferService bulkPrintTransferService;

    public TransferCasePostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        TransferCaseNotificationsService transferCaseNotificationsService,
        TransferCaseDocumentPublishService transferCaseDocumentPublishService,
        BulkPrintTransferService bulkPrintTransferService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.transferCaseNotificationsService = transferCaseNotificationsService;
        this.transferCaseDocumentPublishService = transferCaseDocumentPublishService;
        this.bulkPrintTransferService = bulkPrintTransferService;
    }

    public CallbackResponse transferToCCBC(CallbackParams callbackParams) {
        return completeCaseTransfer(callbackParams,
            transferCaseDocumentPublishService::publishLetterToDefendant,
            transferCaseNotificationsService::sendTransferToCcbcEmail,
            bulkPrintTransferService::updateCaseDataWithHandOffDate
        );
    }

    public CallbackResponse transferToCourt(CallbackParams callbackParams) {
        return completeCaseTransfer(callbackParams, transferCaseDocumentPublishService::publishCaseDocuments,
            transferCaseNotificationsService::sendTransferToCourtEmail, bulkPrintTransferService::updateCaseData);
    }

    private CallbackResponse completeCaseTransfer(CallbackParams callbackParams,
            TriFunction<CCDCase, String, Claim, CCDCase> transferCaseDocumentPublishService,
            BiConsumer<CCDCase, Claim> sendEmailNotifications, UnaryOperator<CCDCase> updateCaseData) {

        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        ccdCase = bulkPrintTransferService.transferCase(ccdCase, claim, authorisation,
            transferCaseDocumentPublishService, sendEmailNotifications, updateCaseData);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

}
