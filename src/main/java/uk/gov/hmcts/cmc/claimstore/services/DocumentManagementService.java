package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.document.DocumentDownloadClientApi;
import uk.gov.hmcts.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.document.DocumentUploadClientApi;
import uk.gov.hmcts.document.domain.Document;
import uk.gov.hmcts.document.domain.UploadResponse;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

import java.net.URI;

import static java.util.Collections.singletonList;

@Service
public class DocumentManagementService {

    static final String APPLICATION_PDF = "application/pdf";
    static final String PDF_EXTENSION = ".pdf";
    static final String FILES_NAME = "files";
    private final DocumentMetadataDownloadClientApi documentMetadataDownloadApi;
    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final DocumentUploadClientApi documentUploadClientApi;
    private final ClaimService claimService;

    @Autowired
    public DocumentManagementService(
        final DocumentMetadataDownloadClientApi documentMetadataDownloadApi,
        final DocumentDownloadClientApi documentDownloadClientApi,
        final DocumentUploadClientApi documentUploadClientApi,
        final ClaimService claimService
    ) {
        this.documentMetadataDownloadApi = documentMetadataDownloadApi;
        this.documentDownloadClientApi = documentDownloadClientApi;
        this.documentUploadClientApi = documentUploadClientApi;
        this.claimService = claimService;
    }

    public void storeClaimN1Form(final String authorisation, final long claimId,
                                 final String claimReferenceNumber, final byte[] n1FormPdf) {
        final Document document = uploadDocument(authorisation, claimReferenceNumber, n1FormPdf);
        claimService.linkDocumentManagement(claimId, URI.create(document.links.self.href).getPath());
    }

    private Document uploadDocument(final String authorisation, final String claimReferenceNumber,
                                    final byte[] n1FormPdf) {
        final String originalFileName = claimReferenceNumber + PDF_EXTENSION;
        final MultipartFile file = new InMemoryMultipartFile(FILES_NAME, originalFileName, APPLICATION_PDF, n1FormPdf);
        final UploadResponse response = documentUploadClientApi.upload(authorisation, singletonList(file));

        return response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public byte[] getClaimN1Form(final String authorisation, final String sealedClaimDocumentManagementSelfPath) {
        final Document documentMetadata = documentMetadataDownloadApi.getDocumentMetadata(authorisation,
            sealedClaimDocumentManagementSelfPath);

        final ResponseEntity<Resource> responseEntity = documentDownloadClientApi.downloadBinary(authorisation,
            URI.create(documentMetadata.links.binary.href).getPath());

        return ((ByteArrayResource) (responseEntity.getBody())).getByteArray();
    }
}
