package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.transfercase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;

@Service
public class TransferCaseService {

    private final GeneralLetterService generalLetterService;
    private final DocAssemblyService docAssemblyService;
    private final NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;
    private final String noticeOfTransferSentToCourtTemplateId;
    private final String noticeOfTransferSentToDefendantTemplateId;

    public TransferCaseService(
        GeneralLetterService generalLetterService,
        DocAssemblyService docAssemblyService,
        NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper,
        @Value("${doc_assembly.noticeOfTransferSentToCourtTemplateId}") String noticeOfTransferSentToCourtTemplateId,
        @Value("${doc_assembly.noticeOfTransferSentToDefendantTemplateId}")
            String noticeOfTransferSentToDefendantTemplateId
    ) {
        this.generalLetterService = generalLetterService;
        this.docAssemblyService = docAssemblyService;
        this.noticeOfTransferLetterTemplateMapper = noticeOfTransferLetterTemplateMapper;
        this.noticeOfTransferSentToCourtTemplateId = noticeOfTransferSentToCourtTemplateId;
        this.noticeOfTransferSentToDefendantTemplateId = noticeOfTransferSentToDefendantTemplateId;
    }

    public CCDCase attachNoticeOfTransferLetterToCase(CCDCase ccdCase, String authorisation,
                                                      NoticeOfTransferLetterType noticeOfTransferLetterType) {

        CCDDocument noticeOfTransferLetter = generateNoticeOfTransferLetter(ccdCase, authorisation,
            noticeOfTransferLetterType);

        return generalLetterService.attachGeneralLetterToCase(ccdCase, noticeOfTransferLetter,
            noticeOfTransferLetterType.documentName);
    }

    private CCDDocument generateNoticeOfTransferLetter(CCDCase ccdCase, String authorisation,
                                                       NoticeOfTransferLetterType noticeOfTransferLetterType) {

        String templateId = templateId(noticeOfTransferLetterType);
        DocAssemblyTemplateBody formPayload = noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBody(
            ccdCase, noticeOfTransferLetterType);

        var docAssemblyResponse = docAssemblyService.createLetter(authorisation, templateId, formPayload);

        return CCDDocument.builder().documentUrl(docAssemblyResponse.getRenditionOutputLocation()).build();
    }

    public void sendCaseDocumentsToBulkPrint(CCDCase ccdCase) {
        // TODO Based on GeneralLetterService.printLetter
    }

    public void sendClaimUpdatedEmailToClaimant(CCDCase ccdCase) {
        // TODO
    }

    public void sendClaimUpdatedEmailToDefendant(CCDCase ccdCase) {
        // TODO
    }

    private String templateId(NoticeOfTransferLetterType noticeOfTransferLetterType) {
        switch (noticeOfTransferLetterType) {
            case FOR_COURT:
                return noticeOfTransferSentToCourtTemplateId;
            case FOR_DEFENDANT:
                return noticeOfTransferSentToDefendantTemplateId;
            default:
                throw new IllegalArgumentException(noticeOfTransferLetterType.name());
        }
    }
}
