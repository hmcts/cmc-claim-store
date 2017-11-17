package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class DocumentManagementSealedClaimHandler {
    static final String PDF_EXTENSION = ".pdf";
    static final String APPLICATION_PDF = "application/pdf";

    private final DocumentManagementService documentManagementService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final ClaimService claimService;
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;


    @Autowired
    public DocumentManagementSealedClaimHandler(
        final DocumentManagementService documentManagementService,
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final ClaimService claimService
    ) {
        this.documentManagementService = documentManagementService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.claimService = claimService;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
    }


    @EventListener
    public void uploadCitizenSealedClaimToEvidenceStore(final ClaimIssuedEvent event) {
        final Claim claim = event.getClaim();
        final byte[] n1FormPdf = citizenSealedClaimPdfService.createPdf(claim, event.getSubmitterEmail());
        uploadSealedClaimToEvidenceManagement(event.getAuthorisation(), claim, n1FormPdf);

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
        final byte[] n1FormPdf = legalSealedClaimPdfService.createPdf(event.getClaim());
        uploadSealedClaimToEvidenceManagement(event.getAuthorisation(), event.getClaim(), n1FormPdf);
    }
}
