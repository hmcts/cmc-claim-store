package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;

import static java.util.Collections.singletonList;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class DocumentManagementService {

    private static final String FILES_NAME = "files";

    private final DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final DocumentUploadClientApi documentUploadClient;

    @Autowired
    public DocumentManagementService(
        final DocumentMetadataDownloadClientApi documentMetadataDownloadApi,
        final DocumentDownloadClientApi documentDownloadClientApi,
        final DocumentUploadClientApi documentUploadClientApi
    ) {
        this.documentMetadataDownloadClient = documentMetadataDownloadApi;
        this.documentDownloadClient = documentDownloadClientApi;
        this.documentUploadClient = documentUploadClientApi;
    }

    public String uploadDocument(final String authorisation, final PDF document) {
        return uploadDocument(authorisation, document.getFilename(), document.getBytes(), PDF.CONTENT_TYPE);
    }

    public String uploadDocument(
        final String authorisation,
        final String originalFileName,
        final byte[] documentBytes,
        final String contentType
    ) {
        final MultipartFile file = new InMemoryMultipartFile(FILES_NAME, originalFileName, contentType, documentBytes);
        final UploadResponse response = documentUploadClient.upload(authorisation, singletonList(file));

        final Document document = response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                new DocumentManagementException("Document management failed uploading file" + originalFileName));

        return URI.create(document.links.self.href).getPath();
    }

    public byte[] downloadDocument(final String authorisation, final String documentSelfPath) {
        final Document documentMetadata = documentMetadataDownloadClient.getDocumentMetadata(authorisation,
            documentSelfPath);

        final ResponseEntity<Resource> responseEntity = documentDownloadClient.downloadBinary(authorisation,
            URI.create(documentMetadata.links.binary.href).getPath());

        final ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();
        return resource.getByteArray();
    }
}
