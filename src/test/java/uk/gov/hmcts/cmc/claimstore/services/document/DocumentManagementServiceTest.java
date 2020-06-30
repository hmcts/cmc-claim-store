package uk.gov.hmcts.cmc.claimstore.services.document;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Classification;
import uk.gov.hmcts.reform.document.domain.Document;

import java.net.URI;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementDownloadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(SpringJUnit4ClassRunner.class)
public class DocumentManagementServiceTest {

    private static final ImmutableList<String> USER_ROLES = ImmutableList.of("caseworker-cmc", "citizen");
    private static final String USER_ROLES_JOINED = "caseworker-cmc,citizen";

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

    private DocumentManagementService documentManagementService;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    @Mock
    private ResponseEntity<Resource> responseEntity;
    private final PDF document = new PDF("0000-claim", "test".getBytes(), SEALED_CLAIM);

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn("authString");
        documentManagementService = new DocumentManagementService(
            documentMetadataDownloadClient,
            documentDownloadClient,
            documentUploadClient,
            authTokenGenerator,
            userService,
            appInsights,
            USER_ROLES
        );
    }

    @Test
    public void shouldUploadToDocumentManagement() {
        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);

        when(documentUploadClient
            .upload(anyString(), anyString(), anyString(), eq(USER_ROLES), any(Classification.class), anyList())
        ).thenReturn(successfulDocumentManagementUploadResponse());

        URI documentSelfPath = documentManagementService
            .uploadDocument("authString", document).getDocumentManagementUrl();
        assertNotNull(documentSelfPath);
        assertEquals("/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4", documentSelfPath.getPath());

        verify(documentUploadClient)
            .upload(anyString(), anyString(), anyString(), eq(USER_ROLES), any(Classification.class), anyList());
    }

    @Test
    public void uploadDocumentToDocumentManagementThrowsException() {
        expectedException.expect(DocumentManagementException.class);
        expectedException.expectMessage("Unable to upload document 0000-claim.pdf to document management");

        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));

        String authorisation = "authString";
        when(userService.getUserDetails(eq(authorisation))).thenReturn(userDetails);

        when(documentUploadClient
            .upload(anyString(), anyString(), anyString(), eq(USER_ROLES), any(Classification.class), anyList()))
            .thenReturn(unsuccessfulDocumentManagementUploadResponse());

        documentManagementService.uploadDocument(authorisation, document);
    }

    @Test
    public void shouldDownloadScannedDocumentFromDocumentManagement() {

        URI docUri = setupDocumentDownloadClient();

        ScannedDocument claimDocument = ScannedDocument.builder()
            .documentManagementUrl(docUri)
            .build();

        byte[] pdf = documentManagementService.downloadScannedDocument("auth string", claimDocument);

        assertDocumentDownloadSuccessful(pdf);
    }

    @Test
    public void shouldDownloadDocumentFromDocumentManagement() {

        URI docUri = setupDocumentDownloadClient();

        ClaimDocument claimDocument = ClaimDocument.builder()
            .documentManagementUrl(docUri)
            .documentName("0000-claim")
            .build();
        byte[] pdf = documentManagementService.downloadDocument("auth string", claimDocument);

        assertDocumentDownloadSuccessful(pdf);
    }

    private void assertDocumentDownloadSuccessful(byte[] pdf) {
        assertNotNull(pdf);
        assertArrayEquals("test".getBytes(), pdf);

        verify(documentMetadataDownloadClient)
            .getDocumentMetadata(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), anyString());
        verify(documentDownloadClient)
            .downloadBinary(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), anyString());
    }

    private URI setupDocumentDownloadClient() {
        when(documentMetadataDownloadClient
            .getDocumentMetadata(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), anyString())
        ).thenReturn(successfulDocumentManagementDownloadResponse());

        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

        when(documentDownloadClient
            .downloadBinary(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), anyString())
        ).thenReturn(responseEntity);
        return URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");
    }

    @Test
    public void downloadDocumentFromDocumentManagementThrowException() {
        URI docUri = mock(URI.class);
        expectedException.expect(DocumentManagementException.class);
        expectedException.expectMessage(
            String.format("Unable to download document %s from document management", docUri));

        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);

        when(documentMetadataDownloadClient
            .getDocumentMetadata(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), anyString())
        ).thenReturn(null);

        ClaimDocument claimDocument = ClaimDocument.builder()
            .documentManagementUrl(docUri)
            .documentName("0000-claim")
            .build();
        documentManagementService.downloadDocument("auth string", claimDocument);
    }

    @Test
    public void getDocumentMetaData() {
        URI docUri = URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");

        when(documentMetadataDownloadClient
            .getDocumentMetadata(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), anyString())
        ).thenReturn(successfulDocumentManagementDownloadResponse());

        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

        Document documentMetaData = documentManagementService.getDocumentMetaData("auth string", docUri.getPath());

        assertEquals(72552L, documentMetaData.size);
        assertEquals("000LR002.pdf", documentMetaData.originalDocumentName);

        verify(documentMetadataDownloadClient)
            .getDocumentMetadata(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), anyString());
    }
}
