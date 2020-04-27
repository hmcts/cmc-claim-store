package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CHANGE_CONTACT_DETAILS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
@ConditionalOnProperty({"doc_assembly.url", "feature_toggles.ctsc_enabled"})
public class ChangeContactDetailsCallbackHandler extends CallbackHandler {
    private final ChangeContactDetailsPostProcessor changeContactDetailsPostProcessor;

    private static final List<Role> ROLES = ImmutableList.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(CHANGE_CONTACT_DETAILS);

    @Autowired
    public ChangeContactDetailsCallbackHandler(ChangeContactDetailsPostProcessor changeContactDetailsPostProcessor) {
        this.changeContactDetailsPostProcessor = changeContactDetailsPostProcessor;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
                CallbackType.MID, changeContactDetailsPostProcessor::showNewContactDetails,
                CallbackType.ABOUT_TO_SUBMIT, changeContactDetailsPostProcessor::notifyPartiesViaEmailOrLetter
        );
    }
}


