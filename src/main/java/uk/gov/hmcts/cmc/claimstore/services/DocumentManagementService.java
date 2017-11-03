package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.document.DocumentClientApi;
import uk.gov.hmcts.document.domain.Document;
import uk.gov.hmcts.document.domain.UploadResponse;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

import java.net.URI;

@Service
public class DocumentManagementService {

    private DocumentClientApi documentClientApi;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;
    private final ClaimRepository claimRepository;

    @Autowired
    public DocumentManagementService(final DocumentClientApi documentClientApi,
                                     final LegalSealedClaimPdfService legalSealedClaimPdfService,
                                     final CitizenSealedClaimPdfService citizenSealedClaimPdfService,
                                     final ClaimRepository claimRepository
    ) {
        this.documentClientApi = documentClientApi;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
        this.claimRepository = claimRepository;
    }

    public void storeLegalClaimN1Form(final String authorisation, final Claim claim) {
        final byte[] n1FormPdf = legalSealedClaimPdfService.createPdf(claim);
        final Document document = uploadDocument(authorisation, claim, n1FormPdf);

        claimRepository.linkDocumentManagement(claim.getId(), URI.create(document.links.self.href).getPath(),
            URI.create(document.links.binary.href).getPath());
    }

    public void storeCitizenClaimN1Form(final String authorisation, final Claim claim, final String submitterEmail) {
        final byte[] n1FormPdf = citizenSealedClaimPdfService.createPdf(claim, submitterEmail);
        final Document document = uploadDocument(authorisation, claim, n1FormPdf);

        claimRepository.linkDocumentManagement(claim.getId(), URI.create(document.links.self.href).getPath(),
            URI.create(document.links.binary.href).getPath());
    }

    private Document uploadDocument(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final MultipartFile n1Form = new InMemoryMultipartFile(claim.getReferenceNumber(), n1FormPdf);
        final MultipartFile[] files = {n1Form};
        final UploadResponse response = documentClientApi.upload(authorisation, files, "PRIVATE");

        return response.embedded.documents.stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

}
