package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.DOCUMENT_NAME;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_DOWNLOAD_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_UPLOAD_FAILURE;

@Service
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentManagementService {

    private static final String FILES_NAME = "files";

    private final DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final DocumentUploadClientApi documentUploadClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final String caseWorkerRole;
    private final AppInsights appInsights;

    @Autowired
    public DocumentManagementService(
        DocumentMetadataDownloadClientApi documentMetadataDownloadApi,
        DocumentDownloadClientApi documentDownloadClientApi,
        DocumentUploadClientApi documentUploadClientApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        @Value("${document_management.caseWorkerRole}") String caseWorkerRole,
        AppInsights appInsights
    ) {
        this.documentMetadataDownloadClient = documentMetadataDownloadApi;
        this.documentDownloadClient = documentDownloadClientApi;
        this.documentUploadClient = documentUploadClientApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.caseWorkerRole = caseWorkerRole;
        this.appInsights = appInsights;
    }

    public URI uploadDocument(String authorisation, PDF document) {
        return uploadDocument(authorisation, document.getFilename(), document.getBytes(), PDF.CONTENT_TYPE);
    }

    public URI uploadDocument(
        String authorisation,
        String originalFileName,
        byte[] documentBytes,
        String contentType
    ) {
        try {
            MultipartFile file = new InMemoryMultipartFile(FILES_NAME, originalFileName, contentType, documentBytes);
            UploadResponse response = documentUploadClient.upload(
                authorisation,
                authTokenGenerator.generate(),
                userService.getUserDetails(authorisation).getId(),
                singletonList(file)
            );

            Document document = response.getEmbedded().getDocuments().stream()
                .findFirst()
                .orElseThrow(() ->
                    new DocumentManagementException("Document management failed uploading file" + originalFileName));

            return URI.create(document.links.self.href);
        } catch (Exception ex) {
            appInsights.trackEvent(DOCUMENT_MANAGEMENT_UPLOAD_FAILURE, DOCUMENT_NAME, originalFileName);
            throw new DocumentManagementException(String.format("Unable to upload document %s to document management",
                originalFileName), ex);
        }
    }

    public byte[] downloadDocument(String authorisation, URI documentSelf, String baseFileName) {
        try {
            Document documentMetadata = documentMetadataDownloadClient.getDocumentMetadata(
                authorisation,
                authTokenGenerator.generate(),
                caseWorkerRole,
                documentSelf.getPath()
            );

            ResponseEntity<Resource> responseEntity = documentDownloadClient.downloadBinary(
                authorisation,
                authTokenGenerator.generate(),
                caseWorkerRole,
                URI.create(documentMetadata.links.binary.href).getPath()
            );

            ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();
            return resource.getByteArray();
        } catch (Exception ex) {
            appInsights.trackEvent(DOCUMENT_MANAGEMENT_DOWNLOAD_FAILURE, DOCUMENT_NAME, baseFileName + ".pdf");
            throw new DocumentManagementException(
                String.format("Unable to download document %s from document management", baseFileName), ex);
        }
    }
}
