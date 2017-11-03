package uk.gov.hmcts.cmc.claimstore.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;
import uk.gov.hmcts.document.DocumentClientApi;
import uk.gov.hmcts.document.domain.Document;
import uk.gov.hmcts.document.domain.UploadResponse;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

import java.net.URI;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementServiceTest {

    private DocumentManagementService documentManagementService;

    @Mock
    private DocumentClientApi documentClientApi;
    @Mock
    private CitizenSealedClaimPdfService citizenSealedClaimPdfService;
    @Mock
    private LegalSealedClaimPdfService legalSealedClaimPdfService;
    @Mock
    private ClaimRepository claimRepository;

    @Before
    public void setup() {
        documentManagementService = new DocumentManagementService(documentClientApi, legalSealedClaimPdfService,
            citizenSealedClaimPdfService, claimRepository);
    }

    @Test
    public void shouldUploadLegalSealedClaimForm() {
        //given
        final String authorisationToken = "Open sesame!";
        final Claim claim = SampleClaim.getDefault();
        UploadResponse uploadResponse = getUploadResponse();

        final byte[] legalN1FormPdf = {65, 66, 67, 68};
        when(legalSealedClaimPdfService.createPdf(claim)).thenReturn(legalN1FormPdf);
        final MultipartFile[] files = {new InMemoryMultipartFile(claim.getReferenceNumber(), legalN1FormPdf)};

        when(documentClientApi.upload(authorisationToken, files, "PRIVATE"))
            .thenReturn(uploadResponse);

        final Document.Links links = uploadResponse.embedded.documents.stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new).links;

        //when
        documentManagementService.storeLegalClaimN1Form(authorisationToken, claim);

        //verify
        verify(legalSealedClaimPdfService).createPdf(claim);
        verify(documentClientApi).upload(authorisationToken, files, "PRIVATE");

        verify(claimRepository).linkDocumentManagement(eq(claim.getId()),
            eq(URI.create(links.self.href).getPath()), eq(URI.create(links.binary.href).getPath()));
    }

    private UploadResponse getUploadResponse() {
        final String response = new ResourceReader().read("/document_management_response.json");
        return new JsonMapper(new ObjectMapper()).fromJson(response, UploadResponse.class);
    }

    @Test
    public void shouldUploadCitizenSealedClaimForm() {
        //given
        final String authorisationToken = "Open sesame!";
        final Claim claim = SampleClaim.getDefault();

        final byte[] n1FormPdf = {65, 66, 67, 68};
        final String submitterEmail = "submitter@mail.com";
        when(citizenSealedClaimPdfService.createPdf(claim, submitterEmail)).thenReturn(n1FormPdf);
        final MultipartFile[] files = {new InMemoryMultipartFile(claim.getReferenceNumber(), n1FormPdf)};

        UploadResponse uploadResponse = getUploadResponse();
        when(documentClientApi.upload(authorisationToken, files, "PRIVATE"))
            .thenReturn(uploadResponse);

        final Document.Links links = uploadResponse.embedded.documents.stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new).links;

        //when
        documentManagementService.storeCitizenClaimN1Form(authorisationToken, claim, submitterEmail);

        //verify
        verify(citizenSealedClaimPdfService).createPdf(claim, submitterEmail);
        verify(documentClientApi).upload(authorisationToken, files, "PRIVATE");

        verify(claimRepository).linkDocumentManagement(eq(claim.getId()),
            eq(URI.create(links.self.href).getPath()), eq(URI.create(links.binary.href).getPath()));
    }
}
