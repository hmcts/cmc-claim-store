package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

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

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DRAW_JUDGES_ORDER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.JUDGE;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class DrawJudgeOrderCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = ImmutableList.of(JUDGE);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(DRAW_JUDGES_ORDER);

    private final OrderCreator orderCreator;
    private final OrderPostProcessor orderPostProcessor;

    @Autowired
    public DrawJudgeOrderCallbackHandler(
        OrderCreator orderCreator,
        OrderPostProcessor orderPostProcessor
    ) {
        this.orderCreator = orderCreator;
        this.orderPostProcessor = orderPostProcessor;
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
            CallbackType.ABOUT_TO_START, orderCreator::prepopulateOrder,
            CallbackType.MID, orderCreator::generateOrder,
            CallbackType.ABOUT_TO_SUBMIT, orderPostProcessor::copyDraftToCaseDocument,
            CallbackType.SUBMITTED, orderPostProcessor::notifyPartiesAndPrintOrder
        );
    }
}
