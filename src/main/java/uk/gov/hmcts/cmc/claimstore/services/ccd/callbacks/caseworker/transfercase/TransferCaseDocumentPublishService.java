package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_COURT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_DEFENDANT;

@Service
public class TransferCaseDocumentPublishService {

    private final TransferCaseLetterSender transferCaseLetterSender;
    private final TransferCaseDocumentService transferCaseDocumentService;
    private final DocAssemblyService docAssemblyService;
    private final NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;
    private final String noticeOfTransferSentToCourtTemplateId;
    private final String noticeOfTransferSentToDefendantTemplateId;

    public TransferCaseDocumentPublishService(
        TransferCaseLetterSender transferCaseLetterSender,
        TransferCaseDocumentService transferCaseDocumentService,
        DocAssemblyService docAssemblyService,
        NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper,
        @Value("${doc_assembly.noticeOfTransferSentToCourtTemplateId}")
            String noticeOfTransferSentToCourtTemplateId,
        @Value("${doc_assembly.noticeOfTransferSentToDefendantTemplateId}")
            String noticeOfTransferSentToDefendantTemplateId) {
        this.transferCaseLetterSender = transferCaseLetterSender;
        this.transferCaseDocumentService = transferCaseDocumentService;
        this.docAssemblyService = docAssemblyService;
        this.noticeOfTransferLetterTemplateMapper = noticeOfTransferLetterTemplateMapper;
        this.noticeOfTransferSentToCourtTemplateId = noticeOfTransferSentToCourtTemplateId;
        this.noticeOfTransferSentToDefendantTemplateId = noticeOfTransferSentToDefendantTemplateId;
    }

    public CCDCase publishNoticesOfTransferToCase(CCDCase ccdCase, String authorisation, Claim claim) {

        DocAssemblyTemplateBody formPayloadForCourt =
            noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForCourt(ccdCase, authorisation);

        CCDDocument coverDoc = docAssemblyService.generateDocument(authorisation,
            formPayloadForCourt,
            noticeOfTransferSentToCourtTemplateId)
            .toBuilder()
            .documentFileName(transferCaseDocumentService.buildNoticeOfTransferLetterFileName(ccdCase, FOR_COURT))
            .build();

        CCDCase ccdCaseWithDocuments = ccdCase;

        ccdCaseWithDocuments = transferCaseDocumentService
            .attachNoticeOfTransferForCourt(ccdCaseWithDocuments, coverDoc, authorisation);

        if (!isDefendantLinked(ccdCase)) {
            DocAssemblyTemplateBody formPayloadForDefendant =
                noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForDefendant(
                    ccdCase, authorisation);

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

        return ccdCaseWithDocuments;
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }
}
