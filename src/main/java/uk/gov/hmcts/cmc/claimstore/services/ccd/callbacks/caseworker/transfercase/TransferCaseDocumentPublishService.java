package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.function.BiFunction;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_COURT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.TO_CCBC_FOR_DEFENDANT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.TO_COURT_FOR_DEFENDANT;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildNoticeOfTransferForCourtFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildNoticeOfTransferForDefendantFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildNoticeOfTransferToCcbcForDefendantFileName;

@Service
public class TransferCaseDocumentPublishService {

    private final TransferCaseLetterSender transferCaseLetterSender;
    private final TransferCaseDocumentService transferCaseDocumentService;
    private final DocAssemblyService docAssemblyService;
    private final NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;
    private final String courtLetterTemplateId;
    private final String defendantLetterTemplateId;
    private final String ccbcTransferTemplateId;

    public TransferCaseDocumentPublishService(
        TransferCaseLetterSender transferCaseLetterSender,
        TransferCaseDocumentService transferCaseDocumentService,
        DocAssemblyService docAssemblyService,
        NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper,
        @Value("${doc_assembly.noticeOfTransferSentToCourtTemplateId}") String courtLetterTemplateId,
        @Value("${doc_assembly.noticeOfTransferSentToDefendantTemplateId}") String defendantLetterTemplateId,
        @Value("${doc_assembly.noticeOfTransferToCcbcSentToDefendantTemplateId}") String ccbcTransferTemplateId
    ) {
        this.transferCaseLetterSender = transferCaseLetterSender;
        this.transferCaseDocumentService = transferCaseDocumentService;
        this.docAssemblyService = docAssemblyService;
        this.noticeOfTransferLetterTemplateMapper = noticeOfTransferLetterTemplateMapper;
        this.courtLetterTemplateId = courtLetterTemplateId;
        this.defendantLetterTemplateId = defendantLetterTemplateId;
        this.ccbcTransferTemplateId = ccbcTransferTemplateId;
    }

    public CCDCase publishCaseDocuments(CCDCase ccdCase, String authorisation, Claim claim) {

        CCDCase updated = publishLetterToDefendant(ccdCase, authorisation, claim, TO_COURT_FOR_DEFENDANT,
            defendantLetterTemplateId, noticeOfTransferLetterTemplateMapper::noticeOfTransferLetterBodyForDefendant);

        return publishCaseDocumentsToCourt(updated, authorisation, claim);
    }

    public CCDCase publishDefendentDocuments(CCDCase ccdCase, String authorisation, Claim claim) {
        return publishLetterToDefendant(ccdCase, authorisation, claim, TO_CCBC_FOR_DEFENDANT, ccbcTransferTemplateId,
            noticeOfTransferLetterTemplateMapper::noticeOfTransferToCcbcLetterBodyForDefendant);
    }

    private CCDCase publishLetterToDefendant(CCDCase ccdCase, String authorisation, Claim claim,
                         NoticeOfTransferLetterType letterType, String letterTemplateId,
                         BiFunction<CCDCase, String, DocAssemblyTemplateBody> noticeOfTransferLetterTemplateMapper) {
        if (isDefendantLinked(ccdCase)) {
            return ccdCase;
        }

        DocAssemblyTemplateBody formPayloadForDefendant =
            noticeOfTransferLetterTemplateMapper.apply(ccdCase, authorisation);

        CCDDocument defendantLetter = docAssemblyService.generateDocument(ccdCase,
            authorisation,
            formPayloadForDefendant,
            letterTemplateId)
            .toBuilder()
            .documentFileName(buildNoticeOfTransferLetterFileName(ccdCase, letterType))
            .build();

        transferCaseLetterSender.sendNoticeOfTransferForDefendant(authorisation, defendantLetter, claim);

        return transferCaseDocumentService.attachNoticeOfTransfer(ccdCase, defendantLetter, authorisation);
    }

    private CCDCase publishCaseDocumentsToCourt(CCDCase ccdCase, String authorisation, Claim claim) {
        DocAssemblyTemplateBody formPayloadForCourt =
            noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForCourt(ccdCase, authorisation);

        CCDDocument coverDoc = docAssemblyService.generateDocument(ccdCase,
            authorisation,
            formPayloadForCourt,
            courtLetterTemplateId)
            .toBuilder()
            .documentFileName(buildNoticeOfTransferLetterFileName(ccdCase, FOR_COURT))
            .build();

        transferCaseLetterSender.sendAllCaseDocumentsToCourt(authorisation, ccdCase, claim, coverDoc);

        return transferCaseDocumentService.attachNoticeOfTransfer(ccdCase, coverDoc, authorisation);
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
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
            case TO_COURT_FOR_DEFENDANT:
                basename = buildNoticeOfTransferForDefendantFileBaseName(ccdCase.getPreviousServiceCaseReference());
                break;
            case TO_CCBC_FOR_DEFENDANT:
                basename = buildNoticeOfTransferToCcbcForDefendantFileName(ccdCase.getPreviousServiceCaseReference());
                break;
            default:
                throw new IllegalArgumentException(noticeOfTransferLetterType
                    + " noticeOfTransferLetterType unable to be handled");
        }

        return String.format("%s.pdf", basename);
    }
}
