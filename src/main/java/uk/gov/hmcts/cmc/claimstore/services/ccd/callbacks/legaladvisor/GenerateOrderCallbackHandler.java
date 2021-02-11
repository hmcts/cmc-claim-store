package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ACTION_REVIEW_COMMENTS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.LEGAL_ADVISOR;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class GenerateOrderCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(LEGAL_ADVISOR);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(GENERATE_ORDER, ACTION_REVIEW_COMMENTS);

    private final OrderCreator orderCreator;
    private final OrderPostProcessor orderPostProcessor;

    @Value("${doc_assembly.templateId}")
    private String templateId;

    @Autowired
    public GenerateOrderCallbackHandler(
        OrderCreator orderCreator,
        OrderPostProcessor orderPostProcessor
    ) {
        this.orderCreator = orderCreator;
        this.orderPostProcessor = orderPostProcessor;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, orderCreator::prepopulateOrder,
            CallbackType.MID, orderCreator::generateOrder,
            CallbackType.ABOUT_TO_SUBMIT, orderPostProcessor::persistHearingCourtAndMigrateExpertReport,
            CallbackType.SUBMITTED, orderPostProcessor::notifyPartiesAndPrintOrderOrRaiseAppInsight
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
