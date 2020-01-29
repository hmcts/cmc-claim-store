package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class ResetRpaCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private final CaseDetailsConverter caseDetailsConverter;
    private CaseMapper caseMapper;

    @Autowired
    public ResetRpaCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::requestMoreTimeOnPaper
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(CaseEvent.RESET_RPA);
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private AboutToStartOrSubmitCallbackResponse requestMoreTimeOnPaper(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claim = convertCallbackToClaim(callbackRequest);

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder builder
            = AboutToStartOrSubmitCallbackResponse.builder();
        AboutToStartOrSubmitCallbackResponse response = builder
            .data(caseDetailsConverter.convertToMap(caseMapper.to(claim)))
            .build();
        return response;
    }

    private Claim convertCallbackToClaim(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
    }
}
