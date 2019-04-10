package uk.gov.hmcts.cmc.claimstore.services.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Classification;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.DOCUMENT_NAME;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_DOWNLOAD_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_UPLOAD_FAILURE;

public class DocumentManagementService {

    private final Logger logger = LoggerFactory.getLogger(DocumentManagementService.class);
    private static final String FILES_NAME = "files";

    private final DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final DocumentUploadClientApi documentUploadClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final AppInsights appInsights;

    public DocumentManagementService(
        DocumentMetadataDownloadClientApi documentMetadataDownloadApi,
        DocumentDownloadClientApi documentDownloadClientApi,
        DocumentUploadClientApi documentUploadClientApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        AppInsights appInsights
    ) {
        this.documentMetadataDownloadClient = documentMetadataDownloadApi;
        this.documentDownloadClient = documentDownloadClientApi;
        this.documentUploadClient = documentUploadClientApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.appInsights = appInsights;
    }

    @Retryable(value = {DocumentManagementException.class}, backoff = @Backoff(delay = 200))
    public URI uploadDocument(String authorisation, PDF document) {
        String originalFileName = document.getFilename();
        try {
            MultipartFile file
                = new InMemoryMultipartFile(FILES_NAME, originalFileName, PDF.CONTENT_TYPE, document.getBytes());

            UserDetails userDetails = userService.getUserDetails(authorisation);
            UploadResponse response = documentUploadClient.upload(
                authorisation,
                authTokenGenerator.generate(),
                userDetails.getId(),
                userDetails.getRoles(),
                Classification.RESTRICTED,
                singletonList(file)
            );

            Document uploadedDocument = response.getEmbedded().getDocuments().stream()
                .findFirst()
                .orElseThrow(() ->
                    new DocumentManagementException("Document management failed uploading file" + originalFileName));

            return URI.create(uploadedDocument.links.self.href);
        } catch (Exception ex) {
            throw new DocumentManagementException(String.format("Unable to upload document %s to document management",
                originalFileName), ex);
        }
    }

    @Recover
    public URI logUploadDocumentFailure(DocumentManagementException exception, String authorisation, PDF document) {
        String filename = document.getFilename();
        String errorMessage = String.format(
            "After retry : unable to upload document %s into document management due to %s",
            filename, exception.getMessage()
        );

        logger.info(errorMessage, exception);
        appInsights.trackEvent(DOCUMENT_MANAGEMENT_UPLOAD_FAILURE, DOCUMENT_NAME, filename);
        throw exception;
    }

    @Retryable(value = DocumentManagementException.class, backoff = @Backoff(delay = 200))
    public byte[] downloadDocument(String authorisation, URI documentSelf, String baseFileName) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            String roles = userDetails.getRoles().stream().collect(Collectors.joining(","));
            Document documentMetadata = documentMetadataDownloadClient.getDocumentMetadata(
                authorisation,
                authTokenGenerator.generate(),
                roles,
                userDetails.getId(),
                documentSelf.getPath()
            );

            ResponseEntity<Resource> responseEntity = documentDownloadClient.downloadBinary(
                authorisation,
                authTokenGenerator.generate(),
                roles,
                userDetails.getId(),
                URI.create(documentMetadata.links.binary.href).getPath()
            );

            ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();
            return resource.getByteArray();
        } catch (Exception ex) {
            throw new DocumentManagementException(
                String.format("Unable to download document %s from document management", baseFileName), ex);
        }
    }

    @Recover
    public byte[] logDownloadDocumentFailure(
        DocumentManagementException exception,
        String authorisation,
        URI documentSelf,
        String baseFileName
    ) {
        String filename = baseFileName + ".pdf";

        String errorMessage = String.format(
            "Failure: unable to download document %s from document management due to %s",
            filename, exception.getMessage()
        );

        logger.info(errorMessage, exception);
        appInsights.trackEvent(DOCUMENT_MANAGEMENT_DOWNLOAD_FAILURE, DOCUMENT_NAME, filename);
        throw exception;
    }
}
