package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.ORDER_DIRECTIONS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class OrderPostProcessor {
    private final Clock clock;
    private final OrderDrawnNotificationService orderDrawnNotificationService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final LegalOrderService legalOrderService;
    private final HearingCourtDetailsFinder hearingCourtDetailsFinder;

    public OrderPostProcessor(
        Clock clock,
        OrderDrawnNotificationService orderDrawnNotificationService,
        CaseDetailsConverter caseDetailsConverter,
        LegalOrderService legalOrderService,
        HearingCourtDetailsFinder hearingCourtDetailsFinder
    ) {
        this.clock = clock;
        this.orderDrawnNotificationService = orderDrawnNotificationService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.legalOrderService = legalOrderService;
        this.hearingCourtDetailsFinder = hearingCourtDetailsFinder;
    }

    public CallbackResponse copyDraftToCaseDocument(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        CCDDocument draftOrderDoc = Optional.ofNullable(ccdCase.getDraftOrderDoc())
            .orElseThrow(() -> new CallbackException("Draft order not present"));

        HearingCourt hearingCourt = Optional.ofNullable(ccdCase.getHearingCourt())
            .map(hearingCourtDetailsFinder::findHearingCourtAddress)
            .orElseGet(() -> HearingCourt.builder().build());

        CCDCase updatedCase = ccdCase.toBuilder()
            .caseDocuments(updateCaseDocumentsWithOrder(ccdCase, draftOrderDoc))
            .directionOrder(CCDDirectionOrder.builder()
                .createdOn(nowInUTC())
                .hearingCourtName(hearingCourt.getName())
                .hearingCourtAddress(hearingCourt.getAddress())
                .build())
            .build();

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(updatedCase))
            .build();
    }

    public CallbackResponse notifyPartiesAndPrintOrder(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        notifyParties(claim);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return printOrder(authorisation, claim, ccdCase);
    }

    private void notifyParties(Claim claim) {
        orderDrawnNotificationService.notifyClaimant(claim);
        orderDrawnNotificationService.notifyDefendant(claim);
    }

    private CallbackResponse printOrder(String authorisation, Claim claim, CCDCase ccdCase) {
        CCDDocument draftOrderDoc = ccdCase.getDraftOrderDoc();
        legalOrderService.print(authorisation, claim, draftOrderDoc);
        return SubmittedCallbackResponse.builder().build();
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
