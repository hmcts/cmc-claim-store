package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.net.URI;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

@Component
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class ClaimDocumentGenerator {

    private final DocumentManagementService documentManagementService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private CaseRepository caseRepository;

    public ClaimDocumentGenerator(
        DocumentManagementService documentManagementService,
        SealedClaimPdfService sealedClaimPdfService,
        CaseRepository caseRepository) {
        this.documentManagementService = documentManagementService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.caseRepository = caseRepository;
    }

    @SuppressWarnings("squid:S3655")
    public byte[] downloadOrGenerateAndUpload(Claim claim, String authorisation) {
        if (claim.getSealedClaimDocument().isPresent()) {
            URI documentSelf = claim.getSealedClaimDocument().get();
            return documentManagementService.downloadDocument(authorisation, documentSelf);
        }

        byte[] documentSupplier = sealedClaimPdfService.createPdf(claim);
        PDF document = new PDF(buildSealedClaimFileBaseName(claim.getReferenceNumber()), documentSupplier);

        URI documentUri = documentManagementService.uploadDocument(authorisation, document);
        caseRepository.linkSealedClaimDocument(authorisation, claim, documentUri);

        return document.getBytes();
    }

    @EventListener
    public void onClaimIssued(RepresentedClaimIssuedEvent event) {
        //downloadOrGenerateAndUpload(event.getClaim(), event.getAuthorisation());
    }

    @EventListener
    public void onClaimIssued(CitizenClaimIssuedEvent event) {
        //downloadOrGenerateAndUpload(event.getClaim(), event.getAuthorisation());
    }
}
