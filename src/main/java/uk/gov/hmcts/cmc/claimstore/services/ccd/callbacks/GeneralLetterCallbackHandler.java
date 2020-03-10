package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_GENERAL_LETTER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class GeneralLetterCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(ISSUE_GENERAL_LETTER);

    @Autowired
    public GeneralLetterCallbackHandler() {

    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.MID, this::generateLetter,
            CallbackType.ABOUT_TO_SUBMIT, this::printLetter
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

    public CallbackResponse printLetter(CallbackParams callbackParams) {
        return null;
    }

    public CallbackResponse generateLetter(CallbackParams callbackParams) {
        return null;
    }
}
