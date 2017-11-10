package uk.gov.hmcts.cmc.claimstore.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;
import uk.gov.hmcts.document.DocumentDownloadClientApi;
import uk.gov.hmcts.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.document.DocumentUploadClientApi;
import uk.gov.hmcts.document.domain.Document;
import uk.gov.hmcts.document.domain.UploadResponse;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService.APPLICATION_PDF;
import static uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService.FILES_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService.PDF_EXTENSION;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementServiceTest {

    private DocumentManagementService documentManagementService;

    @Mock
    private DocumentUploadClientApi documentUploadClientApi;
    @Mock
    private DocumentMetadataDownloadClientApi documentMetadataDownloadApi;
    @Mock
    private DocumentDownloadClientApi documentDownloadClientApi;
    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ResponseEntity<Resource> responseEntity;

    @Before
    public void setup() {
        documentManagementService = new DocumentManagementService(documentMetadataDownloadApi,
            documentDownloadClientApi, documentUploadClientApi, claimRepository);
    }

    @Test
    public void shouldUploadSealedClaimForm() {
        //given
        final String authorisationToken = "Open sesame!";
        final Claim claim = SampleClaim.getDefault();
        final UploadResponse uploadResponse = getUploadResponse();
        final Document.Links links = getLinks(uploadResponse);

        final byte[] legalN1FormPdf = {65, 66, 67, 68};
        final String originalFileName = claim.getReferenceNumber() + PDF_EXTENSION;

        final InMemoryMultipartFile file
            = new InMemoryMultipartFile(FILES_NAME, originalFileName, APPLICATION_PDF, legalN1FormPdf);

        final List<MultipartFile> files = Collections.singletonList(file);

        when(documentUploadClientApi.upload(authorisationToken, files)).thenReturn(uploadResponse);

        //when
        documentManagementService.storeClaimN1Form(authorisationToken, claim, legalN1FormPdf);

        //verify
        verifyDocumentUpload(authorisationToken, claim, links, files);
    }

    @Test
    public void shouldDownloadSealedClaimForm() {
        //given
        final String authorisationToken = "Open sesame!";
        final String selfUri = SampleClaim.SEALED_CLAIM_DOCUMENT_MANAGEMENT_SELF_URL;
        final Claim claim = SampleClaim.builder().withSealedClaimDocumentManagementSelfUrl(selfUri).build();
        final Document document = getUploadResponse().getEmbedded().getDocuments().get(0);
        final String binaryUri = URI.create(document.links.binary.href).getPath();
        final byte[] legalN1FormPdf = {65, 66, 67, 68};
        final Resource resource = new ByteArrayResource(legalN1FormPdf);

        when(documentMetadataDownloadApi.getDocumentMetadata(authorisationToken, selfUri)).thenReturn(document);
        when(documentDownloadClientApi.downloadBinary(authorisationToken, binaryUri)).thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(resource);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        //when
        final byte[] claimN1Form = documentManagementService.getClaimN1Form(authorisationToken, claim, legalN1FormPdf);

        //then
        assertThat(claimN1Form).isNotNull().isEqualTo(legalN1FormPdf);
        //verify
        verifyDocumentDownload(authorisationToken, selfUri, binaryUri);
    }

    @Test
    public void shouldUploadSealedClaimFormWhenBinaryIsNotPresent() {
        //given
        final String authorisationToken = "Open sesame!";
        final String selfUri = SampleClaim.SEALED_CLAIM_DOCUMENT_MANAGEMENT_SELF_URL;
        final Claim claim = SampleClaim.builder().withSealedClaimDocumentManagementSelfUrl(selfUri).build();
        final Document document = getUploadResponse().getEmbedded().getDocuments().get(0);
        final String binaryUri = URI.create(document.links.binary.href).getPath();
        final byte[] legalN1FormPdf = {65, 66, 67, 68};
        final UploadResponse uploadResponse = getUploadResponse();

        final Document.Links links = getLinks(uploadResponse);

        final String originalFileName = claim.getReferenceNumber() + PDF_EXTENSION;

        final InMemoryMultipartFile file
            = new InMemoryMultipartFile(FILES_NAME, originalFileName, APPLICATION_PDF, legalN1FormPdf);

        final List<MultipartFile> files = Collections.singletonList(file);

        when(documentUploadClientApi.upload(authorisationToken, files)).thenReturn(uploadResponse);
        when(documentMetadataDownloadApi.getDocumentMetadata(authorisationToken, selfUri)).thenReturn(document);
        when(documentDownloadClientApi.downloadBinary(authorisationToken, binaryUri)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        //when
        final byte[] claimN1Form = documentManagementService.getClaimN1Form(authorisationToken, claim, legalN1FormPdf);

        //then
        assertThat(claimN1Form).isNotNull().isEqualTo(legalN1FormPdf);
        //verify
        verifyDocumentDownload(authorisationToken, selfUri, binaryUri);
        verifyDocumentUpload(authorisationToken, claim, links, files);
    }

    @Test
    public void shouldUploadClaimDoesNotHaveDocumentManagementUrl() {
        //given
        final String authorisationToken = "Open sesame!";
        final Claim claim = SampleClaim.getDefault();
        final byte[] legalN1FormPdf = {65, 66, 67, 68};
        final UploadResponse uploadResponse = getUploadResponse();

        final Document.Links links = getLinks(uploadResponse);

        final String originalFileName = claim.getReferenceNumber() + PDF_EXTENSION;

        final InMemoryMultipartFile file
            = new InMemoryMultipartFile(FILES_NAME, originalFileName, APPLICATION_PDF, legalN1FormPdf);

        final List<MultipartFile> files = Collections.singletonList(file);

        when(documentUploadClientApi.upload(authorisationToken, files)).thenReturn(uploadResponse);

        //when
        final byte[] claimN1Form = documentManagementService.getClaimN1Form(authorisationToken, claim, legalN1FormPdf);

        //then
        assertThat(claimN1Form).isNotNull().isEqualTo(legalN1FormPdf);
        //verify
        verifyDocumentUpload(authorisationToken, claim, links, files);
    }

    private Document.Links getLinks(final UploadResponse uploadResponse) {
        return uploadResponse.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new).links;
    }

    private void verifyDocumentUpload(final String authorisationToken, final Claim claim,
                                      final Document.Links links, final List<MultipartFile> files) {
        verify(documentUploadClientApi).upload(authorisationToken, files);

        verify(claimRepository).linkDocumentManagement(eq(claim.getId()),
            eq(URI.create(links.self.href).getPath()));
    }

    private void verifyDocumentDownload(final String authorisationToken, final String selfUri, final String binaryUri) {
        verify(documentMetadataDownloadApi).getDocumentMetadata(eq(authorisationToken), eq(selfUri));
        verify(documentDownloadClientApi).downloadBinary(eq(authorisationToken), eq(binaryUri));
    }

    private UploadResponse getUploadResponse() {
        final String response = new ResourceReader().read("/document_management_response.json");
        return new JsonMapper(new ObjectMapper()).fromJson(response, UploadResponse.class);
    }
}
