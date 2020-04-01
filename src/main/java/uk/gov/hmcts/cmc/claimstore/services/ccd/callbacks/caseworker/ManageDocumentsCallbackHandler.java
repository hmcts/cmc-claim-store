package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MANAGE_DOCUMENTS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class ManageDocumentsCallbackHandler  extends CallbackHandler {
    private static final List<Role> ROLES = ImmutableList.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(MANAGE_DOCUMENTS);
    static final String ERROR_MESSAGE = "You need to upload, edit or delete a document to continue.";

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
            CallbackType.MID, this::checkList
        );
    }

    private CallbackResponse checkList(CallbackParams callbackParams) {
        CallbackRequest request = callbackParams.getRequest();

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder builder =
            AboutToStartOrSubmitCallbackResponse.builder();

        if (request.getCaseDetails().equals(request.getCaseDetailsBefore())) {
            builder.errors(Collections.singletonList(ERROR_MESSAGE));
        }

        return builder.build();
    }
}
