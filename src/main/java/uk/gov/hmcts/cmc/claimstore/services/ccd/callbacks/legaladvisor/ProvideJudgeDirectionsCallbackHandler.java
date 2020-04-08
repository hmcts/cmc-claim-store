package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDLaList;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PROVIDE_DIRECTIONS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.JUDGE;

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class ProvideJudgeDirectionsCallbackHandler extends CallbackHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final List<Role> ROLES = ImmutableList.of(JUDGE);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(PROVIDE_DIRECTIONS);
    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public ProvideJudgeDirectionsCallbackHandler(CaseDetailsConverter caseDetailsConverter) {
        this.caseDetailsConverter = caseDetailsConverter;
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
                CallbackType.ABOUT_TO_SUBMIT, this::changeAssignment
        );
    }

    private CallbackResponse changeAssignment(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        logger.info("Provide Directions callback: case {} now being assigned to 'From judge with directions'",
                ccdCase.getExternalId());
        ccdCase.setAssignedTo(CCDLaList.FROM_JUDGE_WITH_DIRECTION);
        Map<String, Object> dataMap = caseDetailsConverter.convertToMap(ccdCase);
        return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(dataMap)
                .build();
    }
}

