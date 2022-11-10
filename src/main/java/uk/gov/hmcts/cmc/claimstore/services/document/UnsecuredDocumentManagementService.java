/*package uk.gov.hmcts.cmc.claimstore.services.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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

@Slf4j
@Service("documentManagementService")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "document_management", name = "secured", havingValue = "false")
public class UnsecuredDocumentManagementService implements DocumentManagementService {

    private final Logger logger = LoggerFactory.getLogger(DocumentManagementService.class);
    private static final String FILES_NAME = "files";
    private static final String OCMC = "OCMC";

    private final DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final DocumentUploadClientApi documentUploadClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final AppInsights appInsights;
    private final List<String> userRoles;

    @Retryable(value = {DocumentManagementException.class}, backoff = @Backoff(delay = 200))
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

    @Override
    public byte[] downloadDocument(String authorisation, ClaimDocument claimDocument) {
        return downloadDocumentByUrl(authorisation, claimDocument.getDocumentManagementUrl().toString());
    }

    @Override
    public byte[] downloadScannedDocument(String authorisation, ScannedDocument scannedDocument) {
        return downloadDocumentByUrl(authorisation, scannedDocument.getDocumentManagementUrl().toString());
    }

    @Retryable(value = DocumentManagementException.class, backoff = @Backoff(delay = 200))
    @Override
    public byte[] downloadDocumentByUrl(String authorisation, String documentPath) {
        byte[] bytesArray = null;
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            String usrRoles = String.join(",", this.userRoles);
            Document documentMetadata = getDocumentMetaData(authorisation, documentPath);

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
                    documentPath), ex);
        }
    }

    public Document getDocumentMetaData(String authorisation, String documentPath) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            String usrRoles = String.join(",", this.userRoles);
            return documentMetadataDownloadClient.getDocumentMetadata(
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
}*/
