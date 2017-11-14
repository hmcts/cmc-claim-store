package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
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

    @Autowired
    public DocumentManagementService(
        final DocumentMetadataDownloadClientApi documentMetadataDownloadApi,
        final DocumentDownloadClientApi documentDownloadClientApi,
        final DocumentUploadClientApi documentUploadClientApi
    ) {
        this.documentMetadataDownloadApi = documentMetadataDownloadApi;
        this.documentDownloadClientApi = documentDownloadClientApi;
        this.documentUploadClientApi = documentUploadClientApi;
    }

    public String uploadSingleDocument(final String authorisation, final String originalFileName,
                                       final byte[] documentBytes, final String contentType) {
        final MultipartFile file = new InMemoryMultipartFile(FILES_NAME, originalFileName, contentType, documentBytes);
        final UploadResponse response = documentUploadClientApi.upload(authorisation, singletonList(file));

        final String message = "Document management failed uploading file" + originalFileName;

        final Document document = response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(() -> new DocumentManagementException(message));

        return URI.create(document.links.self.href).getPath();
    }

    byte[] downloadDocument(final String authorisation, final String sealedClaimDocumentManagementSelfPath) {
        final Document documentMetadata = documentMetadataDownloadApi.getDocumentMetadata(authorisation,
            sealedClaimDocumentManagementSelfPath);

        final ResponseEntity<Resource> responseEntity = documentDownloadClientApi.downloadBinary(authorisation,
            URI.create(documentMetadata.links.binary.href).getPath());

        return ((ByteArrayResource) (responseEntity.getBody())).getByteArray();
    }
}
