package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.document.DocumentDownloadClientApi;
import uk.gov.hmcts.document.DocumentDownloadMetadataApi;
import uk.gov.hmcts.document.DocumentUploadClientApi;
import uk.gov.hmcts.document.domain.Document;
import uk.gov.hmcts.document.domain.UploadResponse;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.Collections;

@Service
public class DocumentManagementService {

    private DocumentDownloadMetadataApi documentDownloadMetadataApi;
    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final DocumentUploadClientApi documentUploadClientApi;
    private final ClaimRepository claimRepository;

    @Autowired
    public DocumentManagementService(
        final DocumentDownloadMetadataApi documentDownloadMetadataApi,
        final DocumentDownloadClientApi documentDownloadClientApi,
        final DocumentUploadClientApi documentUploadClientApi,
        final ClaimRepository claimRepository
    ) {
        this.documentDownloadMetadataApi = documentDownloadMetadataApi;
        this.documentDownloadClientApi = documentDownloadClientApi;
        this.documentUploadClientApi = documentUploadClientApi;
        this.claimRepository = claimRepository;
    }

    public void storeClaimN1Form(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final Document document = uploadDocument(authorisation, claim, n1FormPdf);

        claimRepository.linkDocumentManagement(claim.getId(), URI.create(document.links.self.href).getPath());
    }

    public byte[] getClaimN1Form(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final Document documentMetadata = documentDownloadMetadataApi.getDocumentMetadata(authorisation,
            claim.getSealedClaimDocumentManagementSelfUri());

        final ResponseEntity<Resource> responseEntity = documentDownloadClientApi.downloadBinary(authorisation,
            URI.create(documentMetadata.links.binary.href).getPath());

        if (responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            uploadDocument(authorisation, claim, n1FormPdf);
            return n1FormPdf;
        } else {
            return ((ByteArrayResource) (responseEntity.getBody())).getByteArray();
        }
    }

    private Document uploadDocument(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final MultipartFile file = new InMemoryMultipartFile("files",
            claim.getReferenceNumber() + ".pdf", "application/pdf", n1FormPdf);

        final UploadResponse response = documentUploadClientApi.upload(authorisation, Collections.singletonList(file));

        return response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
