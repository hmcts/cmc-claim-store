package uk.gov.hmcts.cmc.claimstore.services.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.DOCUMENT_NAME;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_DOWNLOAD_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_UPLOAD_FAILURE;

@Service
public class DocumentManagementService {

    private final Logger logger = LoggerFactory.getLogger(DocumentManagementService.class);
    private static final String FILES_NAME = "files";
    private static final String OCMC = "OCMC";
    public static final String MONEY_CLAIM_CASE_TYPE_ID = "MoneyClaimCase";
    public static final String JURISDICTION_ID = "CMC";

    private final AuthTokenGenerator authTokenGenerator;
    private final AppInsights appInsights;
    private final DocumentMetadataDownloadClientApi documentMetadataDownloadApi;
    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final CaseDocumentClient caseDocumentClient;
    private final UserService userService;
    private final List<String> userRoles;

    @Autowired
    public DocumentManagementService(
        DocumentMetadataDownloadClientApi documentMetadataDownloadApi,
        DocumentDownloadClientApi documentDownloadClientApi,
        UserService userService,
        CaseDocumentClient caseDocumentClient,
        AuthTokenGenerator authTokenGenerator,
        AppInsights appInsights,
        @Value("${document_management.userRoles}") List<String> userRoles
    ) {
        this.documentDownloadClientApi = documentDownloadClientApi;
        this.documentMetadataDownloadApi = documentMetadataDownloadApi;
        this.caseDocumentClient = caseDocumentClient;
        this.userService = userService;
        this.authTokenGenerator = authTokenGenerator;
        this.appInsights = appInsights;
        this.userRoles = userRoles;
    }

    @Retryable(value = {DocumentManagementException.class}, backoff = @Backoff(delay = 200))
    public ClaimDocument uploadDocument(String authorisation, PDF pdf) {
        String originalFileName = pdf.getFilename();
        try {
            MultipartFile file
                = new InMemoryMultipartFile(FILES_NAME, originalFileName, PDF.CONTENT_TYPE, pdf.getBytes());

            UploadResponse response = caseDocumentClient.uploadDocuments(authorisation,
                authTokenGenerator.generate(),
                MONEY_CLAIM_CASE_TYPE_ID,
                JURISDICTION_ID,
                singletonList(file));

            uk.gov.hmcts.reform.ccd.document.am.model.Document document = response.getDocuments().stream()
                .findFirst()
                .orElseThrow(() ->
                    new DocumentManagementException("Document management failed uploading file" + originalFileName));

            return ClaimDocument.builder()
                .documentManagementUrl(URI.create(document.links.self.href))
                .documentManagementBinaryUrl(URI.create(document.links.binary.href))
                .documentName(originalFileName)
                .documentHash(document.hashToken)
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

    public byte[] downloadDocument(String authorisation, ClaimDocument claimDocument, boolean isCaseDocument) {
        if (isCaseDocument) {
            return downloadCaseDocumentByUrl(authorisation, claimDocument.getDocumentManagementUrl());
        } else {
            return downloadDocumentByUrl(authorisation, claimDocument.getDocumentManagementUrl());
        }
    }

    public byte[] downloadScannedDocument(String authorisation, ScannedDocument scannedDocument) {
        return downloadCaseDocumentByUrl(authorisation, scannedDocument.getDocumentManagementUrl());
    }

    @Retryable(value = DocumentManagementException.class, backoff = @Backoff(delay = 200))
    private byte[] downloadCaseDocumentByUrl(String authorisation, URI documentManagementUrl) {
        byte[] bytesArray = null;
        try {
            Document documentMetadata
                = caseDocumentClient.getMetadataForDocument(authorisation,
                authTokenGenerator.generate(),
                documentManagementUrl.getPath());

            ResponseEntity<Resource> responseEntity
                = caseDocumentClient.getDocumentBinary(authorisation,
                authTokenGenerator.generate(),
                URI.create(documentMetadata.links.binary.href).getPath().replaceFirst("/", ""));

            ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();
            //noinspection ConstantConditions let the NPE be thrown
            if (resource != null) {
                bytesArray = resource.getByteArray();
            }
            return bytesArray;

        } catch (Exception ex) {
            throw new DocumentManagementException(
                String.format("Unable to download document %s from document management.",
                    documentManagementUrl), ex);
        }
    }

    @Retryable(value = DocumentManagementException.class, backoff = @Backoff(delay = 200))
    private byte[] downloadDocumentByUrl(String authorisation, URI documentManagementUrl) {
        byte[] bytesArray = null;
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            String authToken = authTokenGenerator.generate();
            String usrRoles = String.join(",", this.userRoles);
            uk.gov.hmcts.reform.document.domain.Document documentMetadata
                = documentMetadataDownloadApi.getDocumentMetadata(
                authorisation,
                authToken,
                usrRoles,
                userDetails.getId(),
                documentManagementUrl.getPath()
            );

            ResponseEntity<Resource> responseEntity = documentDownloadClientApi.downloadBinary(
                authorisation,
                authToken,
                usrRoles,
                userDetails.getId(),
                URI.create(documentMetadata.links.binary.href).getPath()
            );

            ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();
            if (resource != null) {
                bytesArray = resource.getByteArray();
            }
            return bytesArray;

        } catch (Exception ex) {
            throw new DocumentManagementException(
                String.format("Unable to download document %s from document management.",
                    documentManagementUrl), ex);
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
            return caseDocumentClient.getMetadataForDocument(authorisation,
                authTokenGenerator.generate(),
                documentPath);
        } catch (Exception ex) {
            throw new DocumentManagementException(
                "Unable to download document from document management.", ex);
        }
    }
}
