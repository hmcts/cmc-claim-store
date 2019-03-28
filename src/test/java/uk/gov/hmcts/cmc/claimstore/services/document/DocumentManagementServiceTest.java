package uk.gov.hmcts.cmc.claimstore.services.document;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Classification;

import java.net.URI;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.DOCUMENT_NAME;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_DOWNLOAD_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DOCUMENT_MANAGEMENT_UPLOAD_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementDownloadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(SpringJUnit4ClassRunner.class)
public class DocumentManagementServiceTest {

    @Mock
    private DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    @Mock
    private DocumentDownloadClientApi documentDownloadClient;
    @Mock
    private DocumentUploadClientApi documentUploadClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserService userService;
    @Mock
    private AppInsights appInsights;
    @InjectMocks
    private DocumentManagementService documentManagementService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private ResponseEntity<Resource> responseEntity;
    private PDF document = new PDF("0000-claim", "test".getBytes(), SEALED_CLAIM);

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn("authString");
        documentManagementService = new DocumentManagementService(documentMetadataDownloadClient,
            documentDownloadClient, documentUploadClient, authTokenGenerator, userService, appInsights);
    }

    @Test
    public void shouldUploadToDocumentManagement() {
        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);

        when(documentUploadClient
            .upload(anyString(), anyString(), anyString(), anyList(), any(Classification.class), anyList())
        ).thenReturn(successfulDocumentManagementUploadResponse());

        URI documentSelfPath = documentManagementService.uploadDocument("authString", document);
        assertNotNull(documentSelfPath);
        assertEquals("/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4", documentSelfPath.getPath());
    }

    @Test
    public void uploadDocumentToDocumentManagementThrowsException() {
        expectedException.expect(DocumentManagementException.class);
        expectedException.expectMessage("Unable to upload document 0000-claim.pdf to document management");
        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        when(documentUploadClient.upload(anyString(), anyString(), anyString(), anyList()))
            .thenReturn(unsuccessfulDocumentManagementUploadResponse());
        documentManagementService.uploadDocument("authString", document);

        verify(documentUploadClient, atLeast(3))
            .upload(anyString(), anyString(), anyString(), anyList());

        verify(appInsights).trackEvent(DOCUMENT_MANAGEMENT_UPLOAD_FAILURE, DOCUMENT_NAME, anyString());
    }

    @Test
    public void shouldDownloadDocumentFromDocumentManagement() {
        URI docUri = URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");

        when(documentMetadataDownloadClient
            .getDocumentMetadata(anyString(), anyString(), anyString(), anyString(), anyString())
        ).thenReturn(successfulDocumentManagementDownloadResponse());

        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

        when(documentDownloadClient.downloadBinary(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(responseEntity);

        byte[] pdf = documentManagementService.downloadDocument("auth string", docUri, "0000-claim");
        assertNotNull(pdf);
        assertArrayEquals("test".getBytes(), pdf);
    }

    @Test
    public void downloadDocumentFromDocumentManagementThrowException() {
        expectedException.expect(DocumentManagementException.class);
        expectedException.expectMessage("Unable to download document 0000-claim from document management");
        URI docUri = URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");

        when(documentMetadataDownloadClient
            .getDocumentMetadata(anyString(), anyString(), anyString(), anyString(), anyString())
        ).thenReturn(null);

        documentManagementService.downloadDocument("auth string", docUri, "0000-claim");
        verify(appInsights).trackEvent(DOCUMENT_MANAGEMENT_DOWNLOAD_FAILURE, DOCUMENT_NAME, anyString());
    }
}
