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
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
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

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.DOCUMENT_NAME;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_DOWNLOAD_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_UPLOAD_FAILURE;

@Service
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentManagementService {

    private static final String FILES_NAME = "files";
    private static final String OCMC = "OCMC";

    private final DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final DocumentUploadClientApi documentUploadClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final String citizenRole;
    private final AppInsights appInsights;

    @Autowired
    public DocumentManagementService(
        DocumentMetadataDownloadClientApi documentMetadataDownloadApi,
        DocumentDownloadClientApi documentDownloadClientApi,
        DocumentUploadClientApi documentUploadClientApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        @Value("${document_management.citizenRole}") String citizenRole,
        AppInsights appInsights
    ) {
        this.documentMetadataDownloadClient = documentMetadataDownloadApi;
        this.documentDownloadClient = documentDownloadClientApi;
        this.documentUploadClient = documentUploadClientApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.citizenRole = citizenRole;
        this.appInsights = appInsights;
    }

    public ClaimDocument uploadDocument(
        String authorisation,
        PDF pdf
    ) {
        final String originalFileName = pdf.getFilename();
        try {
            MultipartFile file = new InMemoryMultipartFile(FILES_NAME, originalFileName,
                PDF.CONTENT_TYPE,
                pdf.getBytes());
            UploadResponse response = documentUploadClient.upload(
                authorisation,
                authTokenGenerator.generate(),
                userService.getUserDetails(authorisation).getId(),
                singletonList(citizenRole),
                Classification.RESTRICTED,
                singletonList(file)
            );

            Document document = response.getEmbedded().getDocuments().stream()
                .findFirst()
                .orElseThrow(() ->
                    new DocumentManagementException("Document management failed uploading file" + originalFileName));

            return ClaimDocument.builder()
                .documentManagementUrl(URI.create(document.links.self.href))
                .documentName(originalFileName)
                .documentType(pdf.getClaimDocumentType())
                .createdDatetime(LocalDateTimeFactory.nowInUTC())
                .size(document.size)
                .createdBy(OCMC)
                .build();
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
                citizenRole,
                userService.getUserDetails(authorisation).getId(),
                documentSelf.getPath()
            );

            ResponseEntity<Resource> responseEntity = documentDownloadClient.downloadBinary(
                authorisation,
                authTokenGenerator.generate(),
                citizenRole,
                userService.getUserDetails(authorisation).getId(),
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
