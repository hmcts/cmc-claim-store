package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.defendant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DISPUTE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class DisputesAllCallbackHandler  extends CallbackHandler {
    private static final List<Role> ROLES = ImmutableList.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(DISPUTE);

    private final CaseDetailsConverter caseDetailsConverter;
    private final IntentionToProceedNotificationService intentionToProceedNotificationService;

    @Autowired
    public DisputesAllCallbackHandler(CaseDetailsConverter caseDetailsConverter,
                                      IntentionToProceedNotificationService intentionToProceedNotificationService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.intentionToProceedNotificationService = intentionToProceedNotificationService;
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
            CallbackType.SUBMITTED, this::notifyCaseworkers
        );
    }

    private CallbackResponse notifyCaseworkers(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        intentionToProceedNotificationService.notifyCaseworkers(claim);

        return SubmittedCallbackResponse.builder().build();
    }
}
