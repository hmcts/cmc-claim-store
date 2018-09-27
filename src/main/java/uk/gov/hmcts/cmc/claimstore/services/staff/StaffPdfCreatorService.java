package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.PDF;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimantResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForJudgementFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSettlementReachedFileBaseName;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Component
public class StaffPdfCreatorService {

    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final ClaimantResponseReceiptService claimantResponseReceiptService;

    @Autowired
    public StaffPdfCreatorService(DefendantResponseReceiptService defendantResponseReceiptService,
                                  SealedClaimPdfService sealedClaimPdfService,
                                  SettlementAgreementCopyService settlementAgreementCopyService,
                                  CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
                                  ClaimantResponseReceiptService claimantResponseReceiptService) {

        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.claimantResponseReceiptService = claimantResponseReceiptService;
    }

    public EmailAttachment createClaimantResponsePdfAttachment(Claim claim) {
        byte[] claimantResponse = claimantResponseReceiptService.createPdf(claim);
        requireNonNull(claimantResponse);

        return pdf(claimantResponse, buildClaimantResponseFileBaseName(claim.getReferenceNumber()) + PDF);
    }

    public EmailAttachment createResponsePdfAttachment(Claim claim) {
        byte[] defendantResponse = defendantResponseReceiptService.createPdf(claim);
        requireNonNull(defendantResponse);

        return pdf(defendantResponse, buildResponseFileBaseName(claim.getReferenceNumber()) + PDF);
    }

    public EmailAttachment createSettlementReachedPdfAttachment(Claim claim) {
        byte[] settlementPdf = settlementAgreementCopyService.createPdf(claim);
        requireNonNull(settlementPdf);

        return pdf(settlementPdf, buildSettlementReachedFileBaseName(claim.getReferenceNumber()) + PDF);
    }

    public EmailAttachment createSealedClaimPdfAttachment(Claim claim) {
        byte[] sealedClaimPdf = sealedClaimPdfService.createPdf(claim);
        requireNonNull(sealedClaimPdf);

        return pdf(sealedClaimPdf, buildSealedClaimFileBaseName(claim.getReferenceNumber()) + PDF);
    }

    public EmailAttachment generateCountyCourtJudgmentPdf(Claim claim) {
        byte[] generatedPdf = countyCourtJudgmentPdfService.createPdf(claim);

        return pdf(
            generatedPdf,
            buildRequestForJudgementFileBaseName(claim.getReferenceNumber(),
                claim.getClaimData().getDefendant().getName()) + PDF
        );
    }
}
