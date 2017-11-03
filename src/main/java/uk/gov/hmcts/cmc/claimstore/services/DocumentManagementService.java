package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.document.DocumentClientApi;
import uk.gov.hmcts.document.domain.Document;
import uk.gov.hmcts.document.domain.UploadResponse;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

import java.net.URI;

import static uk.gov.hmcts.cmc.claimstore.services.documentmanagement.Classification.PRIVATE;

@Service
public class DocumentManagementService {

    private DocumentClientApi documentClientApi;
    private final ClaimRepository claimRepository;

    @Autowired
    public DocumentManagementService(final DocumentClientApi documentClientApi,
                                     final ClaimRepository claimRepository
    ) {
        this.documentClientApi = documentClientApi;
        this.claimRepository = claimRepository;
    }

    public void storeClaimN1Form(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final Document document = uploadDocument(authorisation, claim, n1FormPdf);

        claimRepository.linkDocumentManagement(claim.getId(), URI.create(document.links.self.href).getPath(),
            URI.create(document.links.binary.href).getPath());
    }

    private Document uploadDocument(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final MultipartFile[] files = {new InMemoryMultipartFile(claim.getReferenceNumber(), n1FormPdf)};
        final UploadResponse response = documentClientApi.upload(authorisation, files, PRIVATE.toString());

        return response.embedded.documents.stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

}
