package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.migration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MIGRATE_CASE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;

@Service
@RequiredArgsConstructor
public class CaseMigrationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Arrays.asList(MIGRATE_CASE);
    private static final List<Role> ROLES = Arrays.asList(CASEWORKER); // Need to double check

    private final RetainAndDisposeService retainAndDisposeService;

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_SUBMIT, this::migrateCase
        );
    }

    private CallbackResponse migrateCase(CallbackParams callbackParams) {
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        caseDetails.getData().put("TTL", retainAndDisposeService.calculateTTL(caseDetails, authorisation));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
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
