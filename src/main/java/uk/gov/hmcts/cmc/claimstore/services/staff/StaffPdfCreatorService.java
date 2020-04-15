package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimantResponseFileBaseName;
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
        PDF claimantResponse = claimantResponseReceiptService.createPdf(claim,
            buildClaimantResponseFileBaseName(claim.getReferenceNumber()));
        requireNonNull(claimantResponse);

        return pdf(
            claimantResponse.getBytes(),
            claimantResponse.getFilename());
    }

    public EmailAttachment createResponsePdfAttachment(Claim claim) {
        PDF defendantResponse = defendantResponseReceiptService.createPdf(claim);
        requireNonNull(defendantResponse);

        return pdf(
            defendantResponse.getBytes(),
            defendantResponse.getFilename());
    }

    public EmailAttachment createSettlementReachedPdfAttachment(Claim claim) {
        PDF settlementPdf = settlementAgreementCopyService.createPdf(claim);
        requireNonNull(settlementPdf);

        return pdf(
            settlementPdf.getBytes(),
            settlementPdf.getFilename());
    }

    public EmailAttachment createSealedClaimPdfAttachment(Claim claim) {
        PDF sealedClaimPdf = sealedClaimPdfService.createPdf(claim);
        requireNonNull(sealedClaimPdf);

        return pdf(
            sealedClaimPdf.getBytes(),
            sealedClaimPdf.getFilename());
    }

    public EmailAttachment generateCountyCourtJudgmentPdf(Claim claim) {
        PDF generatedPdf = countyCourtJudgmentPdfService.createPdf(claim);

        return pdf(
            generatedPdf.getBytes(),
            generatedPdf.getFilename()
        );
    }
}
