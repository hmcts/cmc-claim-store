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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;

import static java.util.Collections.singletonList;

@Service
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentManagementService {

    private static final String FILES_NAME = "files";

    private final DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final DocumentUploadClientApi documentUploadClient;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public DocumentManagementService(
        DocumentMetadataDownloadClientApi documentMetadataDownloadApi,
        DocumentDownloadClientApi documentDownloadClientApi,
        DocumentUploadClientApi documentUploadClientApi,
        AuthTokenGenerator authTokenGenerator
    ) {
        this.documentMetadataDownloadClient = documentMetadataDownloadApi;
        this.documentDownloadClient = documentDownloadClientApi;
        this.documentUploadClient = documentUploadClientApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    public String uploadDocument(String authorisation, PDF document) {
        return uploadDocument(authorisation, document.getFilename(), document.getBytes(), PDF.CONTENT_TYPE);
    }

    public String uploadDocument(
        String authorisation,
        String originalFileName,
        byte[] documentBytes,
        String contentType
    ) {
        MultipartFile file = new InMemoryMultipartFile(FILES_NAME, originalFileName, contentType, documentBytes);
        UploadResponse response = documentUploadClient.upload(
            authorisation,
            authTokenGenerator.generate(),
            singletonList(file)
        );

        Document document = response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                new DocumentManagementException("Document management failed uploading file" + originalFileName));

        return URI.create(document.links.self.href).getPath();
    }

    public byte[] downloadDocument(String authorisation, String documentSelfPath) {
        Document documentMetadata = documentMetadataDownloadClient.getDocumentMetadata(
            authorisation,
            authTokenGenerator.generate(),
            documentSelfPath
        );

        ResponseEntity<Resource> responseEntity = documentDownloadClient.downloadBinary(
            authorisation,
            authTokenGenerator.generate(),
            URI.create(documentMetadata.links.binary.href).getPath()
        );

        ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();
        return resource.getByteArray();
    }
}
