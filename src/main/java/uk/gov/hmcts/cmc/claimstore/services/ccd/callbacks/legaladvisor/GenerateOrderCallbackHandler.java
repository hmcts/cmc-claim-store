package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ACTION_REVIEW_COMMENTS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DRAFTED_BY_LEGAL_ADVISOR;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.LEGAL_ADVISOR;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class GenerateOrderCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(LEGAL_ADVISOR);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(GENERATE_ORDER, ACTION_REVIEW_COMMENTS);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final OrderCreator orderCreator;
    private final OrderPostProcessor orderPostProcessor;

    @Value("${doc_assembly.templateId}")
    private String templateId;

    private final CaseDetailsConverter caseDetailsConverter;
    private final AppInsights appInsights;

    @Autowired
    public GenerateOrderCallbackHandler(
        OrderCreator orderCreator,
        OrderPostProcessor orderPostProcessor,
        CaseDetailsConverter caseDetailsConverter,
        AppInsights appInsights
    ) {
        this.orderCreator = orderCreator;
        this.orderPostProcessor = orderPostProcessor;
        this.caseDetailsConverter = caseDetailsConverter;
        this.appInsights = appInsights;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, orderCreator::prepopulateOrder,
            CallbackType.MID, orderCreator::generateOrder,
            CallbackType.ABOUT_TO_SUBMIT, orderPostProcessor::persistHearingCourtAndMigrateExpertReport,
            CallbackType.SUBMITTED, this::raiseAppInsight
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

    private CallbackResponse raiseAppInsight(CallbackParams callbackParams) {
        logger.info("Generate order callback: raise app insight");
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        appInsights.trackEvent(DRAFTED_BY_LEGAL_ADVISOR, REFERENCE_NUMBER, ccdCase.getPreviousServiceCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
