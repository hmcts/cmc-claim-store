package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
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
    private final DirectionOrderService directionOrderService;
    private AppInsights appInsights;

    public OrderPostProcessor(
        Clock clock,
        OrderDrawnNotificationService orderDrawnNotificationService,
        CaseDetailsConverter caseDetailsConverter,
        LegalOrderService legalOrderService,
        AppInsights appInsights,
        DirectionOrderService directionOrderService
    ) {
        this.clock = clock;
        this.orderDrawnNotificationService = orderDrawnNotificationService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.legalOrderService = legalOrderService;
        this.directionOrderService = directionOrderService;
        this.appInsights = appInsights;
    }

    public CallbackResponse copyDraftToCaseDocument(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        CCDDocument draftOrderDoc = Optional.ofNullable(ccdCase.getDraftOrderDoc())
            .orElseThrow(() -> new CallbackException("Draft order not present"));

        HearingCourt hearingCourt = directionOrderService.getHearingCourt(ccdCase);

        CCDCase updatedCase = ccdCase.toBuilder()
            .expertReportPermissionPartyGivenToClaimant(null)
            .expertReportPermissionPartyGivenToDefendant(null)
            .expertReportInstructionClaimant(null)
            .expertReportInstructionDefendant(null)
            .caseDocuments(updateCaseDocumentsWithOrder(ccdCase, draftOrderDoc))
            .directionOrder(CCDDirectionOrder.builder()
                .createdOn(nowInUTC())
                .hearingCourtName(hearingCourt.getName())
                .hearingCourtAddress(hearingCourt.getAddress())
                .build())
            .build();

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(cleanUpDynamicList(updatedCase)))
            .build();
    }

    public CallbackResponse persistHearingCourtAndMigrateExpertReport(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        HearingCourt hearingCourt = directionOrderService.getHearingCourt(ccdCase);

        CCDCase updatedCase = ccdCase.toBuilder()
            .hearingCourtName(hearingCourt.getName())
            .hearingCourtAddress(hearingCourt.getAddress())
            .expertReportPermissionPartyGivenToClaimant(null)
            .expertReportPermissionPartyGivenToDefendant(null)
            .expertReportInstructionClaimant(null)
            .expertReportInstructionDefendant(null)
            .build();

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(cleanUpDynamicList(updatedCase)))
            .build();
    }

    private CCDCase cleanUpDynamicList(CCDCase ccdCase) {
        return ccdCase.toBuilder()
            //CCD cannot currently handle storing values from dynamic lists, is getting re-implemented in RDM-6651
            //Once CCD is fixed we can remove setting the hearing court to null
            .hearingCourt(null)
            .build();
    }

    public CallbackResponse notifyPartiesAndPrintOrder(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        notifyParties(claim);
        raiseAppInsightEvents(CaseEvent.fromValue(callbackParams.getRequest().getEventId()), ccdCase);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return printOrder(authorisation, claim, ccdCase);
    }

    private void raiseAppInsightEvents(CaseEvent caseEvent, CCDCase ccdCase) {
        switch (caseEvent) {
            case DRAW_ORDER:
                appInsights.trackEvent(AppInsightsEvent.DRAW_ORDER, AppInsights.REFERENCE_NUMBER,
                    ccdCase.getPreviousServiceCaseReference());
                break;
            case DRAW_JUDGES_ORDER:
                appInsights.trackEvent(AppInsightsEvent.DRAW_JUDGES_ORDER, AppInsights.REFERENCE_NUMBER,
                    ccdCase.getPreviousServiceCaseReference());
                break;
            default:
                //Empty
                break;
        }
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
