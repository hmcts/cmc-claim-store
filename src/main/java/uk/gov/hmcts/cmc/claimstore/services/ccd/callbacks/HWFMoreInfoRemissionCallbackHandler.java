package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class HWFMoreInfoRemissionCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private static final List<CaseEvent> EVENTS = ImmutableList.of(CaseEvent.MORE_INFO_REQUIRED_FOR_HWF);

    private final CaseDetailsConverter caseDetailsConverter;

    private final DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    private final CaseMapper caseMapper;

    @Autowired
    public HWFMoreInfoRemissionCallbackHandler(CaseDetailsConverter caseDetailsConverter,
                                         DirectionsQuestionnaireDeadlineCalculator deadlineCalculator,
                                         CaseMapper caseMapper) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.deadlineCalculator = deadlineCalculator;
        this.caseMapper = caseMapper;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::hwfupdateInfo
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

    private CallbackResponse hwfupdateInfo(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();

        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();

        if (!FeaturesUtils.isOnlineDQ(claim)) {
            LocalDate deadline = deadlineCalculator.calculate(LocalDateTime.now());
            claim = claim.toBuilder().directionsQuestionnaireDeadline(deadline).build();
        }

        Map<String, Object> dataMap = caseDetailsConverter.convertToMap(caseMapper.to(claim));

        responseBuilder.data(dataMap);

        return responseBuilder.build();
    }
}
