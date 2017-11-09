package uk.gov.hmcts.cmc.claimstore.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;
import uk.gov.hmcts.document.DocumentDownloadClientApi;
import uk.gov.hmcts.document.DocumentUploadClientApi;
import uk.gov.hmcts.document.domain.Document;
import uk.gov.hmcts.document.domain.UploadResponse;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

import java.net.URI;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.document.domain.Classification.PRIVATE;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementServiceTest {

    private DocumentManagementService documentManagementService;

    @Mock
    private DocumentUploadClientApi documentClientApi;
    @Mock
    private DocumentDownloadClientApi documentDownloadClientApi;
    @Mock
    private ClaimRepository claimRepository;

    @Before
    public void setup() {
        documentManagementService = new DocumentManagementService(documentDownloadClientApi, documentClientApi,
            claimRepository);
    }

    @Test
    public void shouldUploadSealedClaimForm() {
        //given
        final String authorisationToken = "Open sesame!";
        final Claim claim = SampleClaim.getDefault();
        UploadResponse uploadResponse = getUploadResponse();

        final byte[] legalN1FormPdf = {65, 66, 67, 68};
        final MultipartFile[] files = {new InMemoryMultipartFile(claim.getReferenceNumber(), legalN1FormPdf)};
        when(documentClientApi.upload(authorisationToken, prepareRequest(files))).thenReturn(uploadResponse);

        final Document.Links links = uploadResponse.embedded.documents.stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new).links;

        //when
        documentManagementService.storeClaimN1Form(authorisationToken, claim, legalN1FormPdf);

        //verify
        verify(documentClientApi).upload(authorisationToken, prepareRequest(files));

        verify(claimRepository).linkDocumentManagement(eq(claim.getId()),
            eq(URI.create(links.self.href).getPath()));
    }

    private UploadResponse getUploadResponse() {
        final String response = new ResourceReader().read("/document_management_response.json");
        return new JsonMapper(new ObjectMapper()).fromJson(response, UploadResponse.class);
    }

    private MultiValueMap prepareRequest(MultipartFile[] files) {
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            parameters.add("files", file);
        }
        parameters.add("classification", PRIVATE.toString());
        return parameters;
    }

}
