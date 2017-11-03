package uk.gov.hmcts.cmc.claimstore.services.documentmanagement;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.document.DocumentClientApi;
import uk.gov.hmcts.document.domain.UploadResponse;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

import static uk.gov.hmcts.cmc.claimstore.services.documentmanagement.Classification.PRIVATE;

@Component
public class DocumentManagementService {

    private final DocumentClientApi documentClientApi;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;

    public DocumentManagementService(final DocumentClientApi documentClientApi,
                                     final LegalSealedClaimPdfService legalSealedClaimPdfService
    ) {
        this.documentClientApi = documentClientApi;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
    }

    public UploadResponse storeLegalSealedClaimForm(final String authorisation, final Claim claim) {
        final byte[] sealedClaimPdf = legalSealedClaimPdfService.createPdf(claim);
        MultipartFile file = new InMemoryMultipartFile(claim.getReferenceNumber(), sealedClaimPdf);
        return documentClientApi.upload(authorisation, new MultipartFile[] {file}, PRIVATE.toString());
    }
}
