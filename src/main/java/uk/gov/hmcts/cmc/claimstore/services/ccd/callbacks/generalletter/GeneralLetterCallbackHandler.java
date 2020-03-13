package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

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

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_GENERAL_LETTER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class GeneralLetterCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(ISSUE_GENERAL_LETTER);
    private final GeneralLetterService generalLetterService;

    @Autowired
    public GeneralLetterCallbackHandler(
        GeneralLetterService generalLetterService) {
        this.generalLetterService = generalLetterService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.MID, generalLetterService::createAndPreview,
            CallbackType.ABOUT_TO_SUBMIT, generalLetterService::sendToPrint
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
