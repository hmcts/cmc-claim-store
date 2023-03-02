package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDReviewOrDrawOrder;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor.OrderDrawnNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.document.domain.Document;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.ORDER_DIRECTIONS;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DRAFTED_BY_LEGAL_ADVISOR;
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
    private final DocumentManagementService documentManagementService;
    private final ClaimService claimService;
    private AppInsights appInsights;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String REVIEW_OR_DRAW_ORDER = "reviewOrDrawOrder";
    public static final String LA_DRAW_ORDER = "LA_DRAW_ORDER";

    public OrderPostProcessor(
        Clock clock,
        OrderDrawnNotificationService orderDrawnNotificationService,
        CaseDetailsConverter caseDetailsConverter,
        LegalOrderService legalOrderService,
        AppInsights appInsights,
        DirectionOrderService directionOrderService,
        DocumentManagementService documentManagementService,
        ClaimService claimService
    ) {
        this.clock = clock;
        this.orderDrawnNotificationService = orderDrawnNotificationService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.legalOrderService = legalOrderService;
        this.directionOrderService = directionOrderService;
        this.appInsights = appInsights;
        this.documentManagementService = documentManagementService;
        this.claimService = claimService;
    }

    public CallbackResponse copyDraftToCaseDocument(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        CCDDocument draftOrderDoc = Optional.ofNullable(ccdCase.getDraftOrderDoc())
            .orElseThrow(() -> new CallbackException("Draft order not present"));

        HearingCourt hearingCourt = directionOrderService.getHearingCourt(ccdCase);

        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        Document documentMetadata = documentManagementService.getDocumentMetaData(
            authorisation,
            URI.create(draftOrderDoc.getDocumentUrl()).getPath()
        );

        CCDCase updatedCase = ccdCase.toBuilder()
            .expertReportPermissionPartyGivenToClaimant(null)
            .expertReportPermissionPartyGivenToDefendant(null)
            .expertReportInstructionClaimant(null)
            .expertReportInstructionDefendant(null)
            .caseDocuments(updateCaseDocumentsWithOrder(ccdCase, draftOrderDoc, documentMetadata))
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
        if (callbackParams.getRequest().getCaseDetails().getData() != null
            && LA_DRAW_ORDER.equals(callbackParams.getRequest().getCaseDetails()
            .getData().get(REVIEW_OR_DRAW_ORDER))) {
            return copyDraftToCaseDocument(callbackParams);
        }

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

    public CallbackResponse notifyPartiesAndPrintOrderOrRaiseAppInsight(CallbackParams callbackParams) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        if (CCDReviewOrDrawOrder.LA_DRAW_ORDER.equals(ccdCase.getReviewOrDrawOrder())) {
            logger.info("LA drawing order without judge review for claim: {}",
                ccdCase.getPreviousServiceCaseReference());
            return notifyPartiesAndPrintOrder(callbackParams);
        }
        logger.info("Generate order callback: raise app insight");
        appInsights.trackEvent(DRAFTED_BY_LEGAL_ADVISOR, REFERENCE_NUMBER, ccdCase.getPreviousServiceCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder().build();
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
            case GENERATE_ORDER:
                appInsights.trackEvent(AppInsightsEvent.LA_GENERATE_DRAW_ORDER, AppInsights.REFERENCE_NUMBER,
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
        List<BulkPrintDetails> bulkPrintDetails = legalOrderService.print(authorisation, claim, draftOrderDoc);
        ImmutableList.Builder<BulkPrintDetails> printDetails = ImmutableList.builder();
        printDetails.addAll(claim.getBulkPrintDetails());
        printDetails.addAll(bulkPrintDetails);
        claimService.addBulkPrintDetails(authorisation, printDetails.build(), CaseEvent.ADD_BULK_PRINT_DETAILS, claim);

        return SubmittedCallbackResponse.builder().build();
    }

    private List<CCDCollectionElement<CCDClaimDocument>> updateCaseDocumentsWithOrder(
        CCDCase ccdCase,
        CCDDocument draftOrderDoc,
        Document documentMetaData
    ) {
        CCDCollectionElement<CCDClaimDocument> claimDocument = CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(draftOrderDoc)
                .documentName(draftOrderDoc.getDocumentFileName())
                .createdDatetime(LocalDateTime.now(clock.withZone(UTC_ZONE)))
                .documentType(ORDER_DIRECTIONS)
                .size(documentMetaData.size)
                .build())
            .build();

        return ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
            .addAll(ccdCase.getCaseDocuments())
            .add(claimDocument)
            .build();
    }
}
