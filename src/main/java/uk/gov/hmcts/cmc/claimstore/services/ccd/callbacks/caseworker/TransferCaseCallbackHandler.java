package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.BulkPrintTransferService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TRANSFER_CASE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
@ConditionalOnProperty({"feature_toggles.ctsc_enabled"})
public class TransferCaseCallbackHandler extends CallbackHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(TRANSFER_CASE);
    private final BulkPrintTransferService bulkPrintTransferService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public TransferCaseCallbackHandler(
        BulkPrintTransferService bulkPrintTransferService,
        CaseDetailsConverter caseDetailsConverter
    ) {
        this.bulkPrintTransferService = bulkPrintTransferService;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
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
            .data(ImmutableMap.of(
                "dateOfTransfer", "TODAYS_DATE", // TODO
                "reasonForTransfer", null,
                "nameOfTransferCourt", null,
                "addressOfTransferCourt", null
            ))
            .build();
    }

    private CallbackResponse performBulkPrintTransfer(CallbackParams callbackParams) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());

        try {

            var noticeOfTransferLetter = bulkPrintTransferService.generateNoticeOfTransferLetter(ccdCase);

            bulkPrintTransferService.addNoticeOfTransferToCaseRecordToCaseDocuments(ccdCase, noticeOfTransferLetter);

            if (!isDefendantLinked(ccdCase)) {
                var transferNoticeLetter = bulkPrintTransferService.generateTransferNoticeLetter(ccdCase);

                bulkPrintTransferService.addTransferLetterNoticeToCaseDocuments(ccdCase, transferNoticeLetter);
            }

            bulkPrintTransferService.sendCaseDocumentsToBulkPrint(ccdCase);

            bulkPrintTransferService.sendClaimUpdatedEmailToClaimant(ccdCase);

            if (isDefendantLinked(ccdCase)) {
                bulkPrintTransferService.sendClaimUpdatedEmailToDefendant(ccdCase);
            }

        } catch (Exception e) {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(
                    "There was a technical problem. Some items may not have been sent. You may want to try again."))
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        // TODO
        return false;
    }
}
