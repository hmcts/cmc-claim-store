package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
public class DocumentManagementSealedClaimHandler {
    static final String PDF_EXTENSION = ".pdf";
    static final String APPLICATION_PDF = "application/pdf";

    private final DocumentManagementService documentManagementService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final ClaimService claimService;
    private final boolean documentManagementFeatureEnabled;
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;


    @Autowired
    public DocumentManagementSealedClaimHandler(
        final DocumentManagementService documentManagementService,
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final ClaimService claimService,
        @Value("${feature_toggles.document_management}") final boolean documentManagementFeatureEnabled
    ) {
        this.documentManagementService = documentManagementService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.claimService = claimService;
        this.documentManagementFeatureEnabled = documentManagementFeatureEnabled;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
    }


    @EventListener
    public void uploadCitizenSealedClaimToEvidenceStore(final ClaimIssuedEvent event) {
        if (documentManagementFeatureEnabled) {
            final Claim claim = event.getClaim();
            final byte[] n1FormPdf = citizenSealedClaimPdfService.createPdf(claim, event.getSubmitterEmail());
            uploadSealedClaimToEvidenceManagement(event.getAuthorisation(), claim, n1FormPdf);
        }
    }

    private void uploadSealedClaimToEvidenceManagement(final String authorisation, final Claim claim,
                                                       final byte[] n1FormPdf) {
        final String originalFileName = claim.getReferenceNumber() + PDF_EXTENSION;

        final String documentManagementSelfPath = documentManagementService.uploadDocument(
            authorisation, originalFileName, n1FormPdf, APPLICATION_PDF);

        claimService.linkSealedClaimDocumentManagementPath(claim.getId(), documentManagementSelfPath);
    }

    @EventListener
    public void uploadRepresentativeSealedClaimToEvidenceStore(final RepresentedClaimIssuedEvent event) {
        if (documentManagementFeatureEnabled) {
            final byte[] n1FormPdf = legalSealedClaimPdfService.createPdf(event.getClaim());
            uploadSealedClaimToEvidenceManagement(event.getAuthorisation(), event.getClaim(), n1FormPdf);
        }
    }
}
