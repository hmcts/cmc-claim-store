package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
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

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;

@Service
public class ResumeHelpWithFeeClaimSubmissionCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CITIZEN);
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.UPDATE_HELP_WITH_FEE_CLAIM);
    private final Map<CallbackType, Callback> callbacks = Map.of(
        CallbackType.ABOUT_TO_SUBMIT, this::aboutToSubmit
    );

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ResumeHelpWithFeeClaimSubmissionCallbackHandler(
        CaseDetailsConverter caseDetailsConverter
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
    }

    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return callbacks;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    public CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        logger.info("Resuming claim submission with HelpWithFee for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            ccdCase.getExternalId());
        CCDCase updatedCCDCase = ccdCase.toBuilder()
            .paymentAmount(null)
            .feeAmountInPennies(null)
            .paymentReference(null)
            .paymentDateCreated(null)
            .paymentStatus(null)
            .paymentNextUrl(null)
            .paymentReturnUrl(null)
            .paymentTransactionId(null)
            .paymentFeeId(null)
            .build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(updatedCCDCase))
            .build();
    }
}
