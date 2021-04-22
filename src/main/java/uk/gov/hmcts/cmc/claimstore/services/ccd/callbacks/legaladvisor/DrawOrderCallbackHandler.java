package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.JUDGE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.LEGAL_ADVISOR;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class DrawOrderCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = List.of(LEGAL_ADVISOR, JUDGE);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.DRAW_ORDER);
    private static final String DRAFT_ORDER_DOC = "draftOrderDoc";

    private final CaseDetailsConverter caseDetailsConverter;
    private final OrderPostProcessor orderPostProcessor;
    private final OrderRenderer orderRenderer;

    @Autowired
    public DrawOrderCallbackHandler(
        OrderPostProcessor orderPostProcessor,
        CaseDetailsConverter caseDetailsConverter,
        OrderRenderer orderRenderer
    ) {
        this.orderPostProcessor = orderPostProcessor;
        this.caseDetailsConverter = caseDetailsConverter;
        this.orderRenderer = orderRenderer;
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
        return Map.of(
            CallbackType.ABOUT_TO_START, this::regenerateOrder,
            CallbackType.ABOUT_TO_SUBMIT, orderPostProcessor::copyDraftToCaseDocument,
            CallbackType.SUBMITTED, orderPostProcessor::notifyPartiesAndPrintOrder
        );
    }

    private CallbackResponse regenerateOrder(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        var docAssemblyResponse = orderRenderer.renderLegalAdvisorOrder(ccdCase, authorisation);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(Map.of(
                DRAFT_ORDER_DOC,
                CCDDocument.builder().documentUrl(docAssemblyResponse.getRenditionOutputLocation()).build()
            ))
            .build();
    }
}
