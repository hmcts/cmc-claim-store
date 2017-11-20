package uk.gov.hmcts.cmc.claimstore;

import org.junit.Before;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.documentManagementUploadResponse;

public abstract class DocumentManagementBaseIntegrationTest extends BaseIntegrationTest {
    protected static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};
    private static final Resource resource = new ByteArrayResource(PDF_BYTES);

    @Mock
    private ResponseEntity<Resource> responseEntity;

    @Before
    public void before() {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});

        given(documentUploadClientApi.upload(anyString(), any(List.class)))
            .willReturn(documentManagementUploadResponse());

        given(documentMetadataDownloadApi.getDocumentMetadata(anyString(), anyString()))
            .willReturn(documentManagementUploadResponse().getEmbedded().getDocuments().get(0));

        given(documentDownloadClientApi.downloadBinary(anyString(), anyString())).willReturn(responseEntity);

        given(responseEntity.getBody()).willReturn(resource);
    }
}
