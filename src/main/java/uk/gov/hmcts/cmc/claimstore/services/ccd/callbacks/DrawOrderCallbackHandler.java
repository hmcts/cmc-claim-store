package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor.OrderDrawnNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.ORDER_DIRECTIONS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Service
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DrawOrderCallbackHandler extends CallbackHandler {
    private static final String CASE_DOCUMENTS = "caseDocuments";

    private final Clock clock;
    private final JsonMapper jsonMapper;
    private final OrderDrawnNotificationService orderDrawnNotificationService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final LegalOrderService legalOrderService;

    @Autowired
    public DrawOrderCallbackHandler(
        Clock clock,
        JsonMapper jsonMapper,
        OrderDrawnNotificationService orderDrawnNotificationService,
        CaseDetailsConverter caseDetailsConverter,
        LegalOrderService legalOrderService) {
        this.clock = clock;
        this.jsonMapper = jsonMapper;
        this.orderDrawnNotificationService = orderDrawnNotificationService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.legalOrderService = legalOrderService;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(CaseEvent.DRAW_ORDER);
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::copyDraftToCaseDocument,
            CallbackType.SUBMITTED, this::notifyPartiesAndPrintOrder
        );
    }

    private CallbackResponse notifyPartiesAndPrintOrder(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        Map<String, Object> caseData = caseDetails.getData();
        CCDCase ccdCase = jsonMapper.fromMap(caseData, CCDCase.class);
        notifyParties(claim);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return printOrder(authorisation, claim, ccdCase.getOrderGenerationData());
    }

    private void notifyParties(Claim claim) {
        orderDrawnNotificationService.notifyClaimant(claim);
        orderDrawnNotificationService.notifyDefendant(claim);
    }

    private CallbackResponse printOrder(String authorisation, Claim claim, CCDOrderGenerationData orderGenerationData) {
        CCDDocument draftOrderDoc = orderGenerationData.getDraftOrderDoc();
        legalOrderService.print(authorisation, claim, draftOrderDoc);
        return SubmittedCallbackResponse.builder().build();
    }

    private CallbackResponse copyDraftToCaseDocument(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = jsonMapper.fromMap(callbackRequest.getCaseDetails().getData(), CCDCase.class);

        CCDDocument draftOrderDoc = Optional.ofNullable(ccdCase.getOrderGenerationData())
            .map(CCDOrderGenerationData::getDraftOrderDoc)
            .orElseThrow(() -> new CallbackException("Draft order not present"));

        CCDDirectionOrder directionOrder = Optional.ofNullable(ccdCase.getDirectionOrder())
            .orElseThrow(() -> new CallbackException("Direction order not present"));

        CCDCase updatedCase = ccdCase.toBuilder()
            .caseDocuments(updateCaseDocumentsWithOrder(ccdCase, draftOrderDoc))
            .directionOrder(directionOrder.toBuilder().createdOn(nowInUTC()).build())
            .build();

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(updatedCase))
            .build();
    }

    private List<CCDCollectionElement<CCDClaimDocument>> updateCaseDocumentsWithOrder(
        CCDCase ccdCase,
        CCDDocument draftOrderDoc
    ) {
        CCDCollectionElement<CCDClaimDocument> claimDocument = CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(draftOrderDoc)
                .createdDatetime(LocalDateTime.now(clock.withZone(UTC_ZONE)))
                .documentType(ORDER_DIRECTIONS)
                .build())
            .build();

        return ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
            .addAll(ccdCase.getCaseDocuments())
            .add(claimDocument)
            .build();
    }
}
