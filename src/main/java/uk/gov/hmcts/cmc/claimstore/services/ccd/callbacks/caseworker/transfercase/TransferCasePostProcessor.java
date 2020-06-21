package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.TriFunction;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.function.Function;

import static java.time.LocalDate.now;

@Service
public class TransferCasePostProcessor {

    private final CaseDetailsConverter caseDetailsConverter;
    private final TransferCaseNotificationsService transferCaseNotificationsService;
    private final TransferCaseDocumentPublishService transferCaseDocumentPublishService;

    public TransferCasePostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        TransferCaseNotificationsService transferCaseNotificationsService,
        TransferCaseDocumentPublishService transferCaseDocumentPublishService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.transferCaseNotificationsService = transferCaseNotificationsService;
        this.transferCaseDocumentPublishService = transferCaseDocumentPublishService;
    }

    public CallbackResponse transferToCCBC(CallbackParams callbackParams) {
        return completeCaseTransfer(callbackParams, transferCaseDocumentPublishService::publishDefendentDocuments,
            ccdCase -> ccdCase.toBuilder().dateOfHandoff(now()).build());
    }

    public CallbackResponse transferToCourt(CallbackParams callbackParams) {
        return completeCaseTransfer(callbackParams, transferCaseDocumentPublishService::publishCaseDocuments,
            this::updateCaseData);
    }

    public CallbackResponse completeCaseTransfer(CallbackParams callbackParams,
            TriFunction<CCDCase, String, Claim, CCDCase> transferCaseDocumentPublishService,
            Function<CCDCase, CCDCase> updateCaseData) {

        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        ccdCase = transferCaseDocumentPublishService.apply(ccdCase, authorisation, claim);

        sendEmailNotifications(ccdCase, claim);

        ccdCase = updateCaseData.apply(ccdCase);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private CCDCase updateCaseData(CCDCase ccdCase) {

        return ccdCase.toBuilder()
            .transferContent(ccdCase.getTransferContent().toBuilder().dateOfTransfer(LocalDate.now()).build())
            .build();
    }

    private void sendEmailNotifications(CCDCase ccdCase, Claim claim) {

        transferCaseNotificationsService.sendClaimUpdatedEmailToClaimant(claim);

        if (isDefendantLinked(ccdCase)) {
            transferCaseNotificationsService.sendClaimUpdatedEmailToDefendant(claim);
        }
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }
}
