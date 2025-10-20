package uk.gov.hmcts.cmc.claimstore.services.document;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(SpringJUnit4ClassRunner.class)
public class SecuredDocumentManagementServiceTest {

    private static final ImmutableList<String> USER_ROLES = ImmutableList.of("caseworker-cmc", "citizen");
    private static final String USER_ROLES_JOINED = "caseworker-cmc,citizen";
    private final PDF document = new PDF("0000-claim", "test".getBytes(), SEALED_CLAIM);
    private final Document caseDocument = Document.builder().build();
    @Mock
    private DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    @Mock
    private DocumentDownloadClientApi documentDownloadClient;
    @Mock
    private DocumentUploadRequest documentUploadRequestService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserService userService;
    @Mock
    private UserInfo userInfoService;
    @Mock
    private AppInsights appInsights;
    private SecuredDocumentManagementService securedDocumentManagementService;
    @Mock
    private ResponseEntity<Resource> responseEntity;
    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;
    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn("authString");
        securedDocumentManagementService = new SecuredDocumentManagementService(
            documentMetadataDownloadClient,
            documentDownloadClient,
            authTokenGenerator,
            userService,
            appInsights,
            USER_ROLES,
            caseDocumentClientApi,
            caseDocumentClient
        );
    }

    public void shouldUploadToDocumentManagementAndRaiseExceptionWhenFileEmpty() {
        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);

        when(caseDocumentClientApi
            .uploadDocuments(anyString(),
                anyString(),
                any(DocumentUploadRequest.class)))
            .thenReturn(null).thenThrow(new DocumentUploadException("originalFileName"));
    }

    @Test(expected = DocumentUploadException.class)
    public void shouldUploadToDocumentManagementAndRaiseException() {
        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);

        UploadResponse response = new UploadResponse(List.of(caseDocument));

        when(caseDocumentClientApi
            .uploadDocuments(anyString(),
                anyString(),
                any(DocumentUploadRequest.class)))
            .thenReturn(response);

        securedDocumentManagementService
            .uploadDocument("authString", this.document);
    }

    @Test(expected = RuntimeException.class)
    public void shouldDownloadScannedDocumentFromDocumentManagementWhenResponseEntityIsNull() {

        URI docUri = setupDocumentDownloadClientNullRespEntity();

        ScannedDocument claimDocument = ScannedDocument.builder()
            .documentManagementUrl(docUri)
            .build();

        byte[] pdf = securedDocumentManagementService.downloadScannedDocument("auth string", claimDocument);

        assertDocumentDownloadSuccessful(pdf);
    }

    @Test(expected = DocumentDownloadException.class)
    public void shouldDownloadScannedDocumentFromDocumentManagementWithNoResponseEntity() {

        URI docUri = setupDocumentDownloadClientNullResponseEntity();

        ScannedDocument claimDocument = ScannedDocument.builder()
            .documentManagementUrl(docUri)
            .build();

        byte[] pdf = securedDocumentManagementService.downloadScannedDocument("auth string", claimDocument);

        assertDocumentDownloadSuccessful(pdf);
    }

    @Test
    public void shouldDownloadScannedDocumentFromDocumentManagement() {

        URI docUri = setupDocumentDownloadClient();

        ScannedDocument claimDocument = ScannedDocument.builder()
            .documentManagementUrl(docUri)
            .build();

        byte[] pdf = securedDocumentManagementService.downloadScannedDocument("auth string", claimDocument);

        assertDocumentDownloadSuccessful(pdf);
    }

    @Test
    public void shouldDownloadDocumentFromDocumentManagement() {

        URI docUri = setupDocumentDownloadClient();

        ClaimDocument claimDocument = ClaimDocument.builder()
            .documentManagementUrl(docUri)
            .documentName("0000-claim")
            .build();
        byte[] pdf = securedDocumentManagementService.downloadDocument("auth string", claimDocument);

        assertDocumentDownloadSuccessful(pdf);
    }

    private void assertDocumentDownloadSuccessful(byte[] pdf) {
        assertNotNull(pdf);
        assertArrayEquals("test".getBytes(), pdf);
        verify(caseDocumentClientApi)
            .getDocumentBinary(anyString(), anyString(), any(UUID.class));
    }

    private URI setupDocumentDownloadClient() {
        when(caseDocumentClient.getMetadataForDocument(anyString(), anyString(), anyString()))
            .thenReturn(ResourceLoader.successfulDocumentManagementDownloadResponse());
        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        UserInfo userInfo = UserInfo.builder()
            .familyName("family")
            .name("name")
            .sub("sub")
            .roles(List.of("citizen"))
            .uid("uid")
            .build();

        when(userService.getUserInfo(anyString())).thenReturn(userInfo);
        when(userInfoService.getUid()).thenReturn(userInfo.getUid());
        when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

        when(caseDocumentClientApi.getDocumentBinary(anyString(), anyString(),
            any(UUID.class))).thenReturn(responseEntity);

        when(documentDownloadClient
            .downloadBinary(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), anyString())
        ).thenReturn(responseEntity);
        return URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");
    }

    private URI setupDocumentDownloadClientNullResponseEntity() {
        when(caseDocumentClient.getMetadataForDocument(anyString(), anyString(), anyString()))
            .thenReturn(ResourceLoader.successfulDocumentManagementDownloadResponse());
        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        UserInfo userInfo = UserInfo.builder()
            .familyName("family")
            .name("name")
            .sub("sub")
            .roles(List.of("citizen"))
            .uid("uid")
            .build();

        var respE = caseDocumentClientApi.getDocumentBinary(
            anyString(),
            anyString(),
            any(UUID.class)
        );

        when(userService.getUserInfo(anyString())).thenReturn(userInfo);
        when(userInfoService.getUid()).thenReturn(userInfo.getUid());
        when(documentDownloadClient.downloadBinary(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(respE);
        when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

        when(caseDocumentClientApi.getDocumentBinary(anyString(), anyString(),
            any(UUID.class))).thenReturn(null);

        when(documentDownloadClient
            .downloadBinary(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), anyString())
        ).thenReturn(responseEntity);
        return URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");
    }

    private URI setupDocumentDownloadClientNullRespEntity() {
        when(caseDocumentClient.getMetadataForDocument(anyString(), anyString(), anyString()))
            .thenReturn(ResourceLoader.successfulDocumentManagementDownloadResponse());
        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        UserInfo userInfo = UserInfo.builder()
            .familyName("family")
            .name("name")
            .sub("sub")
            .roles(List.of("citizen"))
            .uid("uid")
            .build();

        when(userService.getUserInfo(anyString())).thenReturn(userInfo);
        when(userInfoService.getUid()).thenReturn(userInfo.getUid());
        when(responseEntity.getBody()).thenReturn(null);

        when(caseDocumentClientApi.getDocumentBinary(anyString(), anyString(),
            any(UUID.class))).thenReturn(responseEntity);

        when(documentDownloadClient
            .downloadBinary(anyString(), anyString(), eq(USER_ROLES_JOINED), anyString(), anyString())
        ).thenReturn(responseEntity);
        return URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");
    }

    @Test(expected = DocumentDownloadException.class)
    public void downloadDocumentFromDocumentManagementThrowException() {
        URI docUri = mock(URI.class);
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
        try {
            securedDocumentManagementService.downloadDocument("auth string", claimDocument);
            Assert.fail("Expected a DocumentManagementException to be thrown");
        } catch (DocumentManagementException expected) {
            assertThat(expected).hasMessage(String.format("Unable to download document %s from document management.",
                docUri));
        }
    }

    @Test
    public void getDocumentMetaData() {
        URI docUri = URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");

        UserDetails userDetails = new UserDetails("id", "mail@mail.com",
            "userFirstName", "userLastName", Collections.singletonList("role"));
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));
        when(caseDocumentClientApi.getMetadataForDocument(anyString(), anyString(), any(UUID.class))).thenReturn(getDocumentMetaDetails());

        Document documentMetaData = securedDocumentManagementService.getDocumentMetaData("auth string", docUri.getPath());

        assertEquals(72552L, documentMetaData.size);
        assertEquals("000LR002.pdf", documentMetaData.originalDocumentName);
    }

    private Document getDocumentMetaDetails() {
        return Document.builder()
            .originalDocumentName("000LR002.pdf")
            .size(72552L)
            .build();
    }
}
