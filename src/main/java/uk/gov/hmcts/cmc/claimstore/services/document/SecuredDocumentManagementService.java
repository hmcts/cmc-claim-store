package uk.gov.hmcts.cmc.claimstore.services.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@Slf4j
@Component
@Qualifier("securedDocumentManagementService")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "document_management", name = "secured", havingValue = "true")
public class SecuredDocumentManagementService implements DocumentManagementService<Document> {

    protected static final int DOC_UUID_LENGTH = 36;
    protected static final String FILES_NAME = "files";
    private static final String CMC = "OCMC";

    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final List<String> userRoles;
    private final CaseDocumentClientApi caseDocumentClientApi;

    @Override
    public ClaimDocument uploadDocument(String authorisation, PDF pdf) {
        String originalFileName = pdf.getFilename();
        log.info("Uploading file {}", originalFileName);
        try {
            MultipartFile file
                = new InMemoryMultipartFile(FILES_NAME, originalFileName, APPLICATION_PDF_VALUE, pdf.getBytes()
            );

            DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(
                Classification.RESTRICTED.toString(),
                "MoneyClaimCase",
                "CMC",
                Collections.singletonList(file)
            );

            UploadResponse response = caseDocumentClientApi.uploadDocuments(
                authorisation,
                authTokenGenerator.generate(),
                documentUploadRequest
            );

            uk.gov.hmcts.reform.ccd.document.am.model.Document document = response.getDocuments().stream()
                .findFirst()
                .orElseThrow(() -> new DocumentUploadException(originalFileName));

            return ClaimDocument.builder()
                .documentManagementUrl(URI.create(document.links.self.href))
                .documentManagementBinaryUrl(URI.create(document.links.binary.href))
                .documentName(originalFileName)
                .documentType(pdf.getClaimDocumentType())
                .createdDatetime(LocalDateTimeFactory.nowInUTC())
                .size(document.size)
                .createdBy(CMC)
                .build();
        } catch (Exception ex) {
            log.error("Failed uploading file {}", originalFileName, ex);
            throw new DocumentUploadException(originalFileName, ex);
        }
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
        log.info("Downloading document {}", documentManagementUrl.getPath());
        try {
            UserInfo userInfo = userService.getUserInfo(authorisation);
            String userRoles = String.join(",", this.userRoles);

            ResponseEntity<Resource> responseEntity = caseDocumentClientApi.getDocumentBinary(
                authorisation,
                authTokenGenerator.generate(),
                getDocumentIdFromSelfHref(documentManagementUrl.toString().substring(documentManagementUrl
                    .getPath().lastIndexOf("/") + 1))
            );

            if (responseEntity == null) {
                Document documentMetadata = getDocumentMetaData(authorisation, documentManagementUrl.getPath());
                responseEntity = documentDownloadClientApi.downloadBinary(
                    authorisation,
                    authTokenGenerator.generate(),
                    userRoles,
                    userInfo.getUid(),
                    URI.create(documentMetadata.links.binary.href).getPath().replaceFirst("/", "")
                );
            }

            return Optional.ofNullable(responseEntity.getBody())
                .map(ByteArrayResource.class::cast)
                .map(ByteArrayResource::getByteArray)
                .orElseThrow(RuntimeException::new);
        } catch (Exception ex) {
            log.error("Failed downloading document {}", documentManagementUrl.getPath(), ex);
            throw new DocumentDownloadException(documentManagementUrl.getPath(), ex);
        }
    }

    public Document getDocumentMetaData(String authorisation, String documentPath) {
        log.info("Getting metadata for file {}", documentPath);

        try {
            return caseDocumentClientApi.getMetadataForDocument(
                authorisation,
                authTokenGenerator.generate(),
                getDocumentIdFromSelfHref(documentPath)
            );

        } catch (Exception ex) {
            log.error("Failed getting metadata for {}", documentPath, ex);
            throw new DocumentDownloadException(documentPath, ex);
        }
    }

    private UUID getDocumentIdFromSelfHref(String selfHref) {
        return UUID.fromString(selfHref.substring(selfHref.length() - DOC_UUID_LENGTH));
    }
}
