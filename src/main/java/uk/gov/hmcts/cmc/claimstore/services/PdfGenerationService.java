package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantPinLetterPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService.PDF_EXTENSION;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class PdfGenerationService {

    public static final String APPLICATION_PDF = "application/pdf";
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;
    private final DefendantPinLetterPdfService defendantPinLetterPdfService;
    private final DocumentManagementService documentManagementService;
    private final ClaimService claimService;
    private final boolean documentManagementFeatureEnabled;

    @Autowired
    public PdfGenerationService(
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService,
        final DefendantPinLetterPdfService defendantPinLetterPdfService,
        final DocumentManagementService documentManagementService,
        final ClaimService claimService,
        @Value("${feature_toggles.document_management}") final boolean documentManagementFeatureEnabled
    ) {
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
        this.defendantPinLetterPdfService = defendantPinLetterPdfService;
        this.documentManagementService = documentManagementService;
        this.claimService = claimService;
        this.documentManagementFeatureEnabled = documentManagementFeatureEnabled;
    }


    public EmailAttachment sealedLegalClaimPdf(String authorisation, Claim claim) {
        final byte[] claimPdf = legalSealedClaimPdfService.createPdf(claim);
        byte[] generatedPdf = getPdfDocument(authorisation, claim, claimPdf);

        return pdf(
            generatedPdf,
            format("%s-sealed-claim.pdf", claim.getReferenceNumber())
        );
    }

    private byte[] getPdfDocument(final String authorisation, final Claim claim, final byte[] n1ClaimPdf) {
        if (documentManagementFeatureEnabled) {
            if (isNotBlank(claim.getSealedClaimDocumentManagementSelfPath())) {
                return documentManagementService.downloadDocument(authorisation,
                    claim.getSealedClaimDocumentManagementSelfPath());
            } else {
                final String originalFileName = claim.getReferenceNumber() + PDF_EXTENSION;

                final String documentManagementSelfPath = documentManagementService.uploadSingleDocument(authorisation,
                    originalFileName, n1ClaimPdf, APPLICATION_PDF);

                claimService.linkDocumentManagement(claim.getId(), documentManagementSelfPath);
            }
        }

        return n1ClaimPdf;
    }

    public EmailAttachment sealedClaimPdf(String authorisation, Claim claim, String submitterEmail) {
        byte[] claimPdf = citizenSealedClaimPdfService.createPdf(claim, submitterEmail);
        byte[] generatedPdf = getPdfDocument(authorisation, claim, claimPdf);

        return pdf(
            generatedPdf,
            format("%s-sealed-claim.pdf", claim.getReferenceNumber())
        );
    }

    public EmailAttachment defendantPinLetterPdf(String authorisation, Claim claim, String defendantPin) {
        byte[] claimPdf = defendantPinLetterPdfService.createPdf(claim, defendantPin);
        byte[] generatedPdf = getPdfDocument(authorisation, claim, claimPdf);

        return pdf(
            generatedPdf,
            format("%s-defendant-pin-letter.pdf", claim.getReferenceNumber())
        );
    }
}
