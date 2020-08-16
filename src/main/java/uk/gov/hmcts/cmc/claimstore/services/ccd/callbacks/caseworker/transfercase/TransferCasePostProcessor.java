package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.elasticsearch.common.TriFunction;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.TransferContent;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

@Service
public class TransferCasePostProcessor {

    private final CaseDetailsConverter caseDetailsConverter;
    private final TransferCaseNotificationsService transferCaseNotificationsService;
    private final TransferCaseDocumentPublishService transferCaseDocumentPublishService;
    private final BulkPrintTransferService bulkPrintTransferService;
    private final DirectionOrderService directionOrderService;

    public TransferCasePostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        TransferCaseNotificationsService transferCaseNotificationsService,
        TransferCaseDocumentPublishService transferCaseDocumentPublishService,
        DirectionOrderService directionOrderService,
        BulkPrintTransferService bulkPrintTransferService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.transferCaseNotificationsService = transferCaseNotificationsService;
        this.transferCaseDocumentPublishService = transferCaseDocumentPublishService;
        this.bulkPrintTransferService = bulkPrintTransferService;
        this.directionOrderService = directionOrderService;
    }

    public CallbackResponse transferToCCBC(CallbackParams callbackParams) {
        return completeCaseTransfer(callbackParams,
            transferCaseDocumentPublishService::publishDefendentDocuments,
            transferCaseNotificationsService::sendTransferToCcbcEmail,
            bulkPrintTransferService::updateCaseDataWithHandOffDate,
            false
        );
    }

    public CallbackResponse transferToCourt(CallbackParams callbackParams) {
        return completeCaseTransfer(callbackParams, transferCaseDocumentPublishService::publishCaseDocuments,
            transferCaseNotificationsService::sendTransferToCourtEmail, bulkPrintTransferService::updateCaseData, true);
    }

    private CallbackResponse completeCaseTransfer(CallbackParams callbackParams,
            TriFunction<CCDCase, String, Claim, CCDCase> transferCaseDocumentPublishService,
            BiConsumer<CCDCase, Claim> sendEmailNotifications, UnaryOperator<CCDCase> updateCaseData,
                                                  boolean isTransferToCourt) {

        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        if (isTransferToCourt) {
            HearingCourt hearingCourt = directionOrderService.getHearingCourt(ccdCase);
            ccdCase = ccdCase.toBuilder()
                .hearingCourtName(hearingCourt.getName())
                .hearingCourtAddress(hearingCourt.getAddress())
                .expertReportPermissionPartyGivenToClaimant(null)
                .expertReportPermissionPartyGivenToDefendant(null)
                .expertReportInstructionClaimant(null)
                .expertReportInstructionDefendant(null)
                .build();
            Address address = Address.builder()
                .line1(hearingCourt.getAddress().getAddressLine1())
                .line2(hearingCourt.getAddress().getAddressLine2())
                .line3(hearingCourt.getAddress().getAddressLine3())
                .city(hearingCourt.getAddress().getPostTown())
                .county(hearingCourt.getAddress().getCounty())
                .postcode(hearingCourt.getAddress().getPostCode()).build();
            TransferContent transferContent = TransferContent.builder()
                .hearingCourtName(hearingCourt.getName())
                .hearingCourtAddress(address)
                .build();
            claim = claim.toBuilder().transferContent(transferContent).build();
        }
        ccdCase = bulkPrintTransferService.transferCase(ccdCase, claim, authorisation,
            transferCaseDocumentPublishService, sendEmailNotifications, updateCaseData);
        if (isTransferToCourt) {
            ccdCase = cleanUpDynamicList(ccdCase);
        }
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private CCDCase cleanUpDynamicList(CCDCase ccdCase) {
        return ccdCase.toBuilder()
            //CCD cannot currently handle storing values from dynamic lists, is getting re-implemented in RDM-6651
            //Once CCD is fixed we can remove setting the hearing court to null
            .hearingCourt(null)
            .build();
    }

    public CallbackResponse processCourt(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .build();
    }
}
