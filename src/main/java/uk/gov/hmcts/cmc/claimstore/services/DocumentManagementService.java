package uk.gov.hmcts.cmc.claimstore.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.document.DocumentDownloadClientApi;
import uk.gov.hmcts.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.document.DocumentUploadClientApi;
import uk.gov.hmcts.document.domain.Document;
import uk.gov.hmcts.document.domain.UploadResponse;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
public class DocumentManagementService {

    protected static final String APPLICATION_PDF = "application/pdf";
    protected static final String PDF_EXTENSION = ".pdf";
    protected static final String FILES_NAME = "files";
    private final DocumentMetadataDownloadClientApi documentMetadataDownloadApi;
    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final DocumentUploadClientApi documentUploadClientApi;
    private final ClaimRepository claimRepository;

    @Autowired
    public DocumentManagementService(
        final DocumentMetadataDownloadClientApi documentMetadataDownloadApi,
        final DocumentDownloadClientApi documentDownloadClientApi,
        final DocumentUploadClientApi documentUploadClientApi,
        final ClaimRepository claimRepository
    ) {
        this.documentMetadataDownloadApi = documentMetadataDownloadApi;
        this.documentDownloadClientApi = documentDownloadClientApi;
        this.documentUploadClientApi = documentUploadClientApi;
        this.claimRepository = claimRepository;
    }

    public void storeClaimN1Form(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final Document document = uploadDocument(authorisation, claim, n1FormPdf);

        claimRepository.linkDocumentManagement(claim.getId(), URI.create(document.links.self.href).getPath());
    }

    public byte[] getClaimN1Form(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        if (StringUtils.isBlank(claim.getSealedClaimDocumentManagementSelfUri())) {
            storeClaimN1Form(authorisation, claim, n1FormPdf);
            return n1FormPdf;
        }

        final Document documentMetadata = documentMetadataDownloadApi.getDocumentMetadata(authorisation,
            claim.getSealedClaimDocumentManagementSelfUri());

        final ResponseEntity<Resource> responseEntity = documentDownloadClientApi.downloadBinary(authorisation,
            URI.create(documentMetadata.links.binary.href).getPath());

        if (responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            storeClaimN1Form(authorisation, claim, n1FormPdf);
            return n1FormPdf;
        } else {
            return ((ByteArrayResource) (responseEntity.getBody())).getByteArray();
        }
    }

    private Document uploadDocument(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final String originalFileName = claim.getReferenceNumber() + PDF_EXTENSION;
        final MultipartFile file = new InMemoryMultipartFile(FILES_NAME, originalFileName, APPLICATION_PDF, n1FormPdf);
        final List<MultipartFile> files = Collections.singletonList(file);
        final UploadResponse response = documentUploadClientApi.upload(authorisation, files);

        return response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
