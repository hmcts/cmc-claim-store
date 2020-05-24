package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.events.BulkPrintTransferEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_COURT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_DEFENDANT;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildNoticeOfTransferForCourtFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildNoticeOfTransferForDefendantFileBaseName;

@Service
public class TransferCasePostProcessor {

    private final CaseDetailsConverter caseDetailsConverter;
    private final GeneralLetterService generalLetterService;
    private final PrintableDocumentService printableDocumentService;
    private final EventProducer eventProducer;
    private final TransferCaseNotificationsService transferCaseNotificationsService;

    public TransferCasePostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        GeneralLetterService generalLetterService,
        PrintableDocumentService printableDocumentService,
        EventProducer eventProducer,
        TransferCaseNotificationsService transferCaseNotificationsService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.generalLetterService = generalLetterService;
        this.printableDocumentService = printableDocumentService;
        this.eventProducer = eventProducer;
        this.transferCaseNotificationsService = transferCaseNotificationsService;
    }

    public CallbackResponse performBulkPrintTransfer(CallbackParams callbackParams) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        ccdCase = publishCaseDocuments(ccdCase, claim, authorisation);

        sendEmailNotifications(ccdCase, claim);

        ccdCase = ccdCase.toBuilder().coverLetterDoc(null).transferContent(null).build();

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private CCDCase publishCaseDocuments(CCDCase ccdCase, Claim claim, String authorisation) {
        CCDCase updated = ccdCase;
        if (!isDefendantLinked(updated)) {
            updated = generalLetterService.publishLetter(updated, claim, authorisation,
                buildNoticeOfTransferLetterFileName(updated, FOR_DEFENDANT));
        }

        Document coverLetterDoc = printableDocumentService.process(updated.getCoverLetterDoc(), authorisation);
        List<BulkPrintTransferEvent.PrintableDocument> caseDocuments = getDocuments(updated, authorisation);
        eventProducer.createBulkPrintTransferEvent(claim, coverLetterDoc, caseDocuments);

        return processData(updated);
    }

    private List<BulkPrintTransferEvent.PrintableDocument> getDocuments(CCDCase ccdCase, String authorisation) {
        return ccdCase.getCaseDocuments().stream()
            .map(CCDCollectionElement::getValue)
            .map(CCDClaimDocument::getDocumentLink)
            .map(d -> this.getPrintableDocument(authorisation, d))
            .collect(Collectors.toList());
    }

    private BulkPrintTransferEvent.PrintableDocument getPrintableDocument(String authorisation, CCDDocument document) {
        Document printableDocument = printableDocumentService.process(document, authorisation);
        return new BulkPrintTransferEvent.PrintableDocument(printableDocument, document.getDocumentFileName());
    }

    private CCDCase processData(CCDCase ccdCase) {
        CCDCase updated = generalLetterService.attachGeneralLetterToCase(ccdCase,
            ccdCase.getCoverLetterDoc(), buildNoticeOfTransferLetterFileName(ccdCase, FOR_COURT));

        return updated.toBuilder()
            .coverLetterDoc(null)
            .transferContent(null)
            .build();
    }

    private void sendEmailNotifications(CCDCase ccdCase, Claim claim) {
        transferCaseNotificationsService.sendClaimUpdatedEmailToClaimant(claim);

        if (isDefendantLinked(ccdCase)) {
            transferCaseNotificationsService.sendClaimUpdatedEmailToDefendant(claim);
        }
    }

    private String buildNoticeOfTransferLetterFileName(
        CCDCase ccdCase,
        NoticeOfTransferLetterType noticeOfTransferLetterType
    ) {
        String basename;

        switch (noticeOfTransferLetterType) {
            case FOR_COURT:
                basename = buildNoticeOfTransferForCourtFileBaseName(ccdCase.getPreviousServiceCaseReference());
                break;
            case FOR_DEFENDANT:
                basename = buildNoticeOfTransferForDefendantFileBaseName(ccdCase.getPreviousServiceCaseReference());
                break;
            default:
                throw new IllegalArgumentException();
        }

        return String.format("%s.pdf", basename);
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }
}
