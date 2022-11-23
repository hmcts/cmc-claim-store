package uk.gov.hmcts.cmc.claimstore.services.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Classification;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.DOCUMENT_NAME;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_DOWNLOAD_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_UPLOAD_FAILURE;

@Slf4j
@RequiredArgsConstructor
@Service("legacyDocumentManagementService")
@ConditionalOnProperty(prefix = "document_management", name = "secured", havingValue = "false")
public class LegacyDocumentManagementService implements DocumentManagementService<Document> {

    private final Logger logger = LoggerFactory.getLogger(DocumentManagementService.class);
    private static final String FILES_NAME = "files";
    private static final String OCMC = "OCMC";

    private final DocumentMetadataDownloadClientApi documentMetadataDownloadClientApi;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final DocumentUploadClientApi documentUploadClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final AppInsights appInsights;
    private final UserService userService;
    private final List<String> userRoles;

    @Override
    public ClaimDocument uploadDocument(String authorisation, PDF pdf) {
        String originalFileName = pdf.getFilename();
        try {
            MultipartFile file
                = new InMemoryMultipartFile(FILES_NAME, originalFileName, PDF.CONTENT_TYPE, pdf.getBytes());

            UserDetails userDetails = userService.getUserDetails(authorisation);
            UploadResponse response = documentUploadClient.upload(
                authorisation,
                authTokenGenerator.generate(),
                userDetails.getId(),
                userRoles,
                Classification.RESTRICTED,
                singletonList(file)
            );

            Document document = response.getEmbedded().getDocuments().stream()
                .findFirst()
                .orElseThrow(() ->
                    new DocumentManagementException("Document management failed uploading file" + originalFileName));

            return ClaimDocument.builder()
                .documentManagementUrl(URI.create(document.links.self.href))
                .documentManagementBinaryUrl(URI.create(document.links.binary.href))
                .documentName(originalFileName)
                .documentType(pdf.getClaimDocumentType())
                .createdDatetime(LocalDateTimeFactory.nowInUTC())
                .size(document.size)
                .createdBy(OCMC)
                .build();
        } catch (Exception ex) {
            throw new DocumentManagementException(String.format("Unable to upload document %s to document management.",
                originalFileName), ex);
        }
    }

    @Recover
    public ClaimDocument logUploadDocumentFailure(
        DocumentManagementException exception,
        String authorisation,
        PDF pdf
    ) {
        String filename = pdf.getFilename();
        logger.warn(" Following exception has occurred {} and cause {} and"
            + "other details are {}", exception.getMessage(), exception.getCause(), exception);
        appInsights.trackEvent(DOCUMENT_MANAGEMENT_UPLOAD_FAILURE, DOCUMENT_NAME, filename);
        throw exception;
    }

    @Override
    public byte[] downloadDocument(String authorisation, ClaimDocument claimDocument) {
        return downloadDocumentByUrl(authorisation, claimDocument.getDocumentManagementUrl());
    }

    @Override
    public byte[] downloadScannedDocument(String authorisation, ScannedDocument scannedDocument) {
        return downloadDocumentByUrl(authorisation, scannedDocument.getDocumentManagementUrl());
    }

    @Override
    public byte[] downloadDocumentByUrl(String authorisation, URI documentManagementUrl) {
        byte[] bytesArray = null;
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            String usrRoles = String.join(",", this.userRoles);
            Document documentMetadata = getDocumentMetaData(authorisation, documentManagementUrl.getPath());

            ResponseEntity<Resource> responseEntity = documentDownloadClient.downloadBinary(
                authorisation,
                authTokenGenerator.generate(),
                usrRoles,
                userDetails.getId(),
                URI.create(documentMetadata.links.binary.href).getPath()
            );

            ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();
            //noinspection ConstantConditions let the NPE be thrown
            if (resource != null) {
                bytesArray = resource.getByteArray();
            }
            return bytesArray;

        } catch (Exception ex) {
            throw new DocumentManagementException(
                String.format("Unable to download document %s from document management.",
                    documentManagementUrl.getPath()), ex);
        }
    }

    @Recover
    public byte[] logDownloadDocumentFailure(
        DocumentManagementException exception,
        String authorisation,
        ClaimDocument claimDocument
    ) {
        String filename = claimDocument.getDocumentName() + ".pdf";
        logger.warn(" Following exception has occurred {} and cause {} and"
            + "other details are {}", exception.getMessage(), exception.getCause(), exception);
        appInsights.trackEvent(DOCUMENT_MANAGEMENT_DOWNLOAD_FAILURE, DOCUMENT_NAME, filename);
        throw exception;
    }

    public Document getDocumentMetaData(String authorisation, String documentPath) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            String usrRoles = String.join(",", this.userRoles);
            return documentMetadataDownloadClientApi.getDocumentMetadata(
                authorisation,
                authTokenGenerator.generate(),
                usrRoles,
                userDetails.getId(),
                documentPath
            );
        } catch (Exception ex) {
            throw new DocumentManagementException(
                "Unable to download document from document management.", ex);
        }
    }
}
