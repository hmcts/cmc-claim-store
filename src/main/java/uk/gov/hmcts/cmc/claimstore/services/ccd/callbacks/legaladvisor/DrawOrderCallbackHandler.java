package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourtDetailsFinder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor.OrderDrawnNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

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
public class DrawOrderCallbackHandler extends CallbackHandler {
    private static final String DRAFT_ORDER_DOC = "draftOrderDoc";

    private final Clock clock;
    private final OrderDrawnNotificationService orderDrawnNotificationService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final LegalOrderService legalOrderService;
    private final HearingCourtDetailsFinder hearingCourtDetailsFinder;
    private final DocAssemblyService docAssemblyService;

    @Autowired
    public DrawOrderCallbackHandler(
        Clock clock,
        OrderDrawnNotificationService orderDrawnNotificationService,
        CaseDetailsConverter caseDetailsConverter,
        LegalOrderService legalOrderService,
        HearingCourtDetailsFinder hearingCourtDetailsFinder,
        DocAssemblyService docAssemblyService
    ) {
        this.clock = clock;
        this.orderDrawnNotificationService = orderDrawnNotificationService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.legalOrderService = legalOrderService;
        this.hearingCourtDetailsFinder = hearingCourtDetailsFinder;
        this.docAssemblyService = docAssemblyService;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(CaseEvent.DRAW_ORDER);
    }

    @Override
    public List<String> getSupportedRoles() {
        return ImmutableList.of("caseworker-cmc-legaladvisor", "caseworker-cmc-judge");
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, this::regenerateOrder,
            CallbackType.ABOUT_TO_SUBMIT, this::copyDraftToCaseDocument,
            CallbackType.SUBMITTED, this::notifyPartiesAndPrintOrder
        );
    }

    private CallbackResponse regenerateOrder(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        DocAssemblyResponse docAssemblyResponse = docAssemblyService.createOrder(ccdCase, authorisation);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.of(
                DRAFT_ORDER_DOC,
                CCDDocument.builder().documentUrl(docAssemblyResponse.getRenditionOutputLocation()).build()
            ))
            .build();
    }

    private CallbackResponse notifyPartiesAndPrintOrder(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        notifyParties(claim);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return printOrder(authorisation, claim, ccdCase.getDirectionOrderData());
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
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        CCDDocument draftOrderDoc = Optional.ofNullable(ccdCase.getDirectionOrderData())
            .map(CCDOrderGenerationData::getDraftOrderDoc)
            .orElseThrow(() -> new CallbackException("Draft order not present"));

        HearingCourt hearingCourt = Optional.ofNullable(ccdCase.getDirectionOrderData().getHearingCourt())
            .map(hearingCourtDetailsFinder::findHearingCourtAddress)
            .orElseGet(() -> HearingCourt.builder().build());

        CCDCase updatedCase = ccdCase.toBuilder()
            .caseDocuments(updateCaseDocumentsWithOrder(ccdCase, draftOrderDoc))
            .directionOrder(CCDDirectionOrder.builder()
                .createdOn(nowInUTC())
                .hearingCourtAddress(hearingCourt.getAddress())
                .build())
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
                .documentName(draftOrderDoc.getDocumentFileName())
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
