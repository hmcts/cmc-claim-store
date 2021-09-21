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
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementDownloadResponse;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(SpringJUnit4ClassRunner.class)
public class DocumentManagementServiceTest {

    private static final ImmutableList<String> USER_ROLES = ImmutableList.of("caseworker-cmc", "citizen");
    private static final String USER_ROLES_JOINED = "caseworker-cmc,citizen";
    private final PDF document = new PDF("0000-claim", "test".getBytes(), SEALED_CLAIM);
    @Mock
    CaseDocumentClient caseDocumentClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private AppInsights appInsights;
    private DocumentManagementService documentManagementService;
    @Mock
    private ResponseEntity<Resource> responseEntity;

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn("authString");
        documentManagementService = new DocumentManagementService(
            caseDocumentClient,
            authTokenGenerator,
            appInsights
        );
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

        verify(caseDocumentClient)
            .getMetadataForDocument(anyString(), anyString(), anyString());
        verify(caseDocumentClient)
            .getDocumentBinary(anyString(), anyString(), anyString());
    }

    private URI setupDocumentDownloadClient() {
        when(caseDocumentClient
            .getMetadataForDocument(anyString(), anyString(), anyString())
        ).thenReturn(successfulDocumentManagementDownloadResponse());

        when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

        when(caseDocumentClient
            .getDocumentBinary(anyString(), anyString(), anyString())
        ).thenReturn(responseEntity);
        return URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");
    }

    @Test
    public void downloadDocumentFromDocumentManagementThrowException() {
        URI docUri = mock(URI.class);

        when(caseDocumentClient
            .getMetadataForDocument(anyString(), anyString(), anyString())
        ).thenReturn(null);

        ClaimDocument claimDocument = ClaimDocument.builder()
            .documentManagementUrl(docUri)
            .documentName("0000-claim")
            .build();
        try {
            documentManagementService.downloadDocument("auth string", claimDocument);
            Assert.fail("Expected a DocumentManagementException to be thrown");
        } catch (DocumentManagementException expected) {
            assertThat(expected).hasMessage(String.format("Unable to download document %s from document management.",
                docUri));
        }
    }

    @Test
    public void getDocumentMetaData() {
        URI docUri = URI.create("http://localhost:8085/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");

        when(caseDocumentClient
            .getMetadataForDocument(anyString(), anyString(), anyString())
        ).thenReturn(successfulDocumentManagementDownloadResponse());

        when(responseEntity.getBody()).thenReturn(new ByteArrayResource("test".getBytes()));

        Document documentMetaData = caseDocumentClient.getMetadataForDocument("auth string", "service auth", docUri.getPath());

        assertEquals(72552L, documentMetaData.size);
        assertEquals("000LR002.pdf", documentMetaData.originalDocumentName);

        verify(caseDocumentClient)
            .getMetadataForDocument(anyString(), anyString(), anyString());
    }
}
