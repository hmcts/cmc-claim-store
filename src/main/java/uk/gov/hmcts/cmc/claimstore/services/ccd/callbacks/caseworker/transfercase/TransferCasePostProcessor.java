package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_COURT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_DEFENDANT;

@Service
public class TransferCasePostProcessor {

    private final CaseDetailsConverter caseDetailsConverter;
    private final TransferCaseNotificationsService transferCaseNotificationsService;
    private final TransferCaseLetterSender transferCaseLetterSender;
    private final TransferCaseDocumentService transferCaseDocumentService;
    private final UserService userService;
    private final DocAssemblyService docAssemblyService;
    private final NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;
    private final String noticeOfTransferSentToCourtTemplateId;
    private final String noticeOfTransferSentToDefendantTemplateId;

    public TransferCasePostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        TransferCaseNotificationsService transferCaseNotificationsService,
        TransferCaseLetterSender transferCaseLetterSender,
        TransferCaseDocumentService transferCaseDocumentService,
        UserService userService,
        DocAssemblyService docAssemblyService,
        NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper,
        @Value("${doc_assembly.noticeOfTransferSentToCourtTemplateId}") String noticeOfTransferSentToCourtTemplateId,
        @Value("${doc_assembly.noticeOfTransferSentToDefendantTemplateId}")
            String noticeOfTransferSentToDefendantTemplateId
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.transferCaseNotificationsService = transferCaseNotificationsService;
        this.transferCaseLetterSender = transferCaseLetterSender;
        this.transferCaseDocumentService = transferCaseDocumentService;
        this.userService = userService;
        this.docAssemblyService = docAssemblyService;
        this.noticeOfTransferLetterTemplateMapper = noticeOfTransferLetterTemplateMapper;
        this.noticeOfTransferSentToCourtTemplateId = noticeOfTransferSentToCourtTemplateId;
        this.noticeOfTransferSentToDefendantTemplateId = noticeOfTransferSentToDefendantTemplateId;
    }

    public CallbackResponse completeCaseTransfer(CallbackParams callbackParams) {

        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        ccdCase = publishNoticesOfTransferToCase(ccdCase, authorisation, claim);

//        sendCaseDocumentsToBulkPrint(authorisation, ccdCase, claim);

        sendEmailNotifications(ccdCase, claim);

        ccdCase = updateCaseData(ccdCase);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private CCDCase publishNoticesOfTransferToCase(CCDCase ccdCase, String authorisation, Claim claim) {
        String caseworkerName = getCaseworkerName(authorisation);

        DocAssemblyTemplateBody formPayloadForCourt =
            noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForCourt(ccdCase, caseworkerName);

        CCDDocument coverDoc = docAssemblyService.generateDocument(authorisation,
            formPayloadForCourt,
            noticeOfTransferSentToCourtTemplateId)
            .toBuilder()
            .documentFileName(transferCaseDocumentService
                .buildNoticeOfTransferLetterFileName(ccdCase, FOR_COURT))
            .build();
        CCDCase ccdCaseWithDocuments = ccdCase;

        if (!isDefendantLinked(ccdCase)) {
            DocAssemblyTemplateBody formPayloadForDefendant =
                noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForDefendant(
                    ccdCase, caseworkerName);

            CCDDocument letterForDefendant = docAssemblyService.generateDocument(authorisation,
                formPayloadForDefendant,
                noticeOfTransferSentToDefendantTemplateId)
                .toBuilder()
                .documentFileName(transferCaseDocumentService
                    .buildNoticeOfTransferLetterFileName(ccdCase, FOR_DEFENDANT))
                .build();

            transferCaseLetterSender.sendNoticeOfTransferForDefendant(authorisation, letterForDefendant, claim);

            ccdCaseWithDocuments = transferCaseDocumentService
                .attachNoticeOfTransferForDefendant(ccdCaseWithDocuments, letterForDefendant, authorisation);
        }

        transferCaseLetterSender.sendAllCaseDocumentsToCourt(authorisation, ccdCaseWithDocuments, claim, coverDoc);
        ccdCaseWithDocuments = transferCaseDocumentService
            .attachNoticeOfTransferForCourt(ccdCaseWithDocuments, coverDoc, authorisation);

        return ccdCaseWithDocuments;
    }

    private CCDCase updateCaseData(CCDCase ccdCase) {

        return ccdCase.toBuilder()
            .transferContent(ccdCase.getTransferContent().toBuilder().dateOfTransfer(LocalDate.now()).build())
            .build();
    }

    private void sendEmailNotifications(CCDCase ccdCase, Claim claim) {

        transferCaseNotificationsService.sendClaimUpdatedEmailToClaimant(claim);

        if (isDefendantLinked(ccdCase)) {
            transferCaseNotificationsService.sendClaimUpdatedEmailToDefendant(claim);
        }
    }

//    private void sendCaseDocumentsToBulkPrint(String authorisation, CCDCase ccdCase, Claim claim) {
//
//        if (!isDefendantLinked(ccdCase)) {
//            transferCaseLetterSender.sendNoticeOfTransferForDefendant(authorisation, ccdCase, claim);
//        }
//
//        transferCaseLetterSender.sendAllCaseDocumentsToCourt(authorisation, ccdCase, claim);
//    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }

    private String getCaseworkerName(String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        return userDetails.getFullName();
    }
}
