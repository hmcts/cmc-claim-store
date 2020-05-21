package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TRANSFER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
@ConditionalOnProperty({"feature_toggles.ctsc_enabled"})    // TODO Prevent CCD error if not enabled
public class TransferCaseCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = List.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = List.of(TRANSFER);
    private final TransferCaseMidProcessor transferCaseMidProcessor;
    private final CaseDetailsConverter caseDetailsConverter;
    private final TransferCasePostProcessor transferCasePostProcessor;

    @Autowired
    public TransferCaseCallbackHandler(
        TransferCaseMidProcessor transferCaseMidProcessor,
        CaseDetailsConverter caseDetailsConverter,
        TransferCasePostProcessor transferCasePostProcessor) {
        this.transferCaseMidProcessor = transferCaseMidProcessor;
        this.caseDetailsConverter = caseDetailsConverter;
        this.transferCasePostProcessor = transferCasePostProcessor;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_START, this::prepopulateData,
            CallbackType.MID, transferCaseMidProcessor::generateNoticeOfTransferLetters,
            CallbackType.ABOUT_TO_SUBMIT, transferCasePostProcessor::performBulkPrintTransfer
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
                "transferContent", CCDTransferContent.builder().dateOfTransfer(LocalDate.now())
            )).build();
    }
}
