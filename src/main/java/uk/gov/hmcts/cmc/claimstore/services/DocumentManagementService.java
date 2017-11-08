package uk.gov.hmcts.cmc.claimstore.services;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.documentmanagement.Classification.PRIVATE;

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
            = {new InMemoryMultipartFile("files", claim.getReferenceNumber()+".pdf", "application/pdf", n1FormPdf)};

        final UploadResponse response = documentClientApi.upload(authorisation, files[0]);

        return response.embedded.documents.stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    private Map<String, Object> prepareRequest(MultipartFile[] files) {
        Map<String, Object> parameters = new HashMap<>();
        for (MultipartFile file : files) {
//            try {
                parameters.put("files", file);
//                    @Override
//                    public String getFilename() {
//                        final String originalFilename = file.getOriginalFilename();
//                        System.out.println(originalFilename);
//                        return originalFilename;
//                    }
//                }));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
        parameters.put("classification", PRIVATE.toString());
        return parameters;
    }
}
