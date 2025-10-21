package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TRANSFER_TO_CCBC;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
@ConditionalOnProperty({"feature_toggles.bulk_print_transfer_enabled"})
public class TransferCaseToCcbcCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = List.of(CASEWORKER);

    private static final List<CaseEvent> EVENTS = List.of(TRANSFER_TO_CCBC);

    private final TransferCasePostProcessor transferCasePostProcessor;

    @Autowired
    public TransferCaseToCcbcCallbackHandler(
        TransferCasePostProcessor transferCasePostProcessor
    ) {
        this.transferCasePostProcessor = transferCasePostProcessor;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_SUBMIT, transferCasePostProcessor::transferToCCBC
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

}
