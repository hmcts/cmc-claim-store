package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TRANSFER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.transfercase.NoticeOfTransferLetterType.FOR_COURT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.transfercase.NoticeOfTransferLetterType.FOR_DEFENDANT;

@Service
@ConditionalOnProperty({"feature_toggles.ctsc_enabled"})
public class TransferCaseCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = List.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = List.of(TRANSFER);
    private final TransferCaseService transferCaseService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public TransferCaseCallbackHandler(
        TransferCaseService transferCaseService,
        CaseDetailsConverter caseDetailsConverter
    ) {
        this.transferCaseService = transferCaseService;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_START, this::prepopulateData,
            CallbackType.ABOUT_TO_SUBMIT, this::performBulkPrintTransfer
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse prepopulateData(CallbackParams callbackParams) {

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(Map.of(
                "dateOfTransfer", Formatting.formatDate(LocalDate.now())
            ))
            .build();
    }

    private CallbackResponse performBulkPrintTransfer(CallbackParams callbackParams) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        ccdCase = transferCaseService.attachNoticeOfTransferLetterToCase(ccdCase, authorisation, FOR_COURT);

        if (!isDefendantLinked(ccdCase)) {
            ccdCase = transferCaseService.attachNoticeOfTransferLetterToCase(ccdCase, authorisation, FOR_DEFENDANT);
        }

        transferCaseService.sendCaseDocumentsToBulkPrint(ccdCase);

        transferCaseService.sendClaimUpdatedEmailToClaimant(ccdCase);

        if (isDefendantLinked(ccdCase)) {
            transferCaseService.sendClaimUpdatedEmailToDefendant(ccdCase);
        }

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }
}
