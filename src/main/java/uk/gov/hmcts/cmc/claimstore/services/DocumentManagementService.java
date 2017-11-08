package uk.gov.hmcts.cmc.claimstore.services;

    import org.apache.commons.io.IOUtils;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.core.io.Resource;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.stereotype.Service;
    import org.springframework.util.LinkedMultiValueMap;
    import org.springframework.util.MultiValueMap;
    import org.springframework.web.multipart.MultipartFile;
    import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
    import uk.gov.hmcts.cmc.claimstore.models.Claim;
    import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
    import uk.gov.hmcts.document.DocumentClientApi;
    import uk.gov.hmcts.document.domain.Document;
    import uk.gov.hmcts.document.domain.UploadResponse;
    import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

    import java.net.URI;

    import static uk.gov.hmcts.document.domain.Classification.PRIVATE;

@Service
public class DocumentManagementService {

    private DocumentClientApi documentClientApi;
    private final ClaimRepository claimRepository;

    @Autowired
    public DocumentManagementService(final DocumentClientApi documentClientApi,
                                     final ClaimRepository claimRepository
    ) {
        this.documentClientApi = documentClientApi;
        this.claimRepository = claimRepository;
    }

    public void storeClaimN1Form(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final Document document = uploadDocument(authorisation, claim, n1FormPdf);

        claimRepository.linkDocumentManagement(claim.getId(), URI.create(document.links.self.href).getPath(),
            URI.create(document.links.binary.href).getPath());
    }

    public byte[] getClaimN1Form(final String authorisation, final Claim claim, final byte[] n1FormPdf)
        throws DocumentManagementException {
        final ResponseEntity<Resource> responseEntity = documentClientApi.downloadBinary(authorisation,
            claim.getSealedClaimDocumentManagementBinaryUri());
        if (responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            uploadDocument(authorisation, claim, n1FormPdf);
            return n1FormPdf;
        } else {
            try {
                final Resource body = responseEntity.getBody();
                return IOUtils.toByteArray(body.getInputStream());
            } catch (Exception e) {
                throw new DocumentManagementException("Failed reading sealed claim form from Document Store ", e);
            }
        }
    }

    private Document uploadDocument(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final MultipartFile[] files
            = {new InMemoryMultipartFile("files", claim.getReferenceNumber() + ".pdf", "application/pdf", n1FormPdf)};

        final UploadResponse response = documentClientApi.upload(authorisation, prepareRequest(files));

        return response.embedded.documents.stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    private MultiValueMap<String, Object> prepareRequest(MultipartFile[] files) {
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            parameters.add("files", file);
        }
        parameters.add("classification", PRIVATE.toString());
        return parameters;
    }
}
