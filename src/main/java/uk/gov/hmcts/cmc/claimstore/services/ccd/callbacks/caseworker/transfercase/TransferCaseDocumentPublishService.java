package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDBulkPrintDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.BulkPrintDetailsMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_COURT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_DEFENDANT;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildNoticeOfTransferForCourtFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildNoticeOfTransferForDefendantFileBaseName;

@Service
public class TransferCaseDocumentPublishService {

    private final TransferCaseLetterSender transferCaseLetterSender;
    private final TransferCaseDocumentService transferCaseDocumentService;
    private final DocAssemblyService docAssemblyService;
    private final NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;
    private final String courtLetterTemplateId;
    private final String defendantLetterTemplateId;
    private final BulkPrintDetailsMapper bulkPrintDetailsMapper;

    public TransferCaseDocumentPublishService(
        TransferCaseLetterSender transferCaseLetterSender,
        TransferCaseDocumentService transferCaseDocumentService,
        DocAssemblyService docAssemblyService,
        NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper,
        @Value("${doc_assembly.noticeOfTransferSentToCourtTemplateId}") String courtLetterTemplateId,
        @Value("${doc_assembly.noticeOfTransferSentToDefendantTemplateId}") String defendantLetterTemplateId,
        BulkPrintDetailsMapper bulkPrintDetailsMapper
    ) {
        this.transferCaseLetterSender = transferCaseLetterSender;
        this.transferCaseDocumentService = transferCaseDocumentService;
        this.docAssemblyService = docAssemblyService;
        this.noticeOfTransferLetterTemplateMapper = noticeOfTransferLetterTemplateMapper;
        this.courtLetterTemplateId = courtLetterTemplateId;
        this.defendantLetterTemplateId = defendantLetterTemplateId;
        this.bulkPrintDetailsMapper = bulkPrintDetailsMapper;
    }

    public CCDCase publishCaseDocuments(CCDCase ccdCase, String authorisation, Claim claim) {

        CCDCase updated = publishLetterToDefendant(ccdCase, authorisation, claim);

        return publishCaseDocumentsToCourt(updated, authorisation, claim);
    }

    private CCDCase publishLetterToDefendant(CCDCase ccdCase, String authorisation, Claim claim) {
        if (isDefendantLinked(ccdCase)) {
            return ccdCase;
        }

        DocAssemblyTemplateBody formPayloadForDefendant =
            noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForDefendant(
                ccdCase, authorisation);

        CCDDocument defendantLetter = docAssemblyService.generateDocument(authorisation,
            formPayloadForDefendant,
            defendantLetterTemplateId)
            .toBuilder()
            .documentFileName(buildNoticeOfTransferLetterFileName(ccdCase, FOR_DEFENDANT))
            .build();

        BulkPrintDetails bulkPrintDetails
            = transferCaseLetterSender.sendNoticeOfTransferForDefendant(authorisation, defendantLetter, claim);

        CCDCase updated = addToBulkPrintDetails(ccdCase, bulkPrintDetails);
        return transferCaseDocumentService.attachNoticeOfTransfer(updated, defendantLetter, authorisation);
    }

    private CCDCase publishCaseDocumentsToCourt(CCDCase ccdCase, String authorisation, Claim claim) {
        DocAssemblyTemplateBody formPayloadForCourt =
            noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForCourt(ccdCase, authorisation);

        CCDDocument coverDoc = docAssemblyService.generateDocument(authorisation,
            formPayloadForCourt,
            courtLetterTemplateId)
            .toBuilder()
            .documentFileName(buildNoticeOfTransferLetterFileName(ccdCase, FOR_COURT))
            .build();

        BulkPrintDetails bulkPrintDetails
            = transferCaseLetterSender.sendAllCaseDocumentsToCourt(authorisation, ccdCase, claim, coverDoc);

        CCDCase updated = addToBulkPrintDetails(ccdCase, bulkPrintDetails);

        return transferCaseDocumentService.attachNoticeOfTransfer(updated, coverDoc, authorisation);
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }

    public CCDCase addToBulkPrintDetails(
        CCDCase ccdCase,
        BulkPrintDetails input
    ) {
        ImmutableList.Builder<CCDCollectionElement<CCDBulkPrintDetails>> printDetails = ImmutableList.builder();
        printDetails.addAll(ccdCase.getBulkPrintDetails());
        printDetails.add(bulkPrintDetailsMapper.to(input));

        return ccdCase.toBuilder().bulkPrintDetails(printDetails.build()).build();
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
                throw new IllegalArgumentException(noticeOfTransferLetterType
                    + " noticeOfTransferLetterType unable to be handled");
        }

        return String.format("%s.pdf", basename);
    }
}
