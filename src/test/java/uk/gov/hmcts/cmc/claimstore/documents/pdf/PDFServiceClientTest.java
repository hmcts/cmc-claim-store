package uk.gov.hmcts.cmc.claimstore.documents.pdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import uk.gov.hmcts.reform.pdf.service.client.exception.PDFServiceClientException;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PDFServiceClientTest {

    private static final URI BASE_URL = URI.create("http://pdf-service");
    private static final URI PDFS_ENDPOINT = URI.create("http://pdf-service/pdfs");
    private static final byte[] TEMPLATE = "<html>{{ name }}</html>".getBytes();
    private static final Map<String, Object> PLACEHOLDERS = Map.of("name", "John");

    @Mock
    private RestOperations restOperations;

    private PDFServiceClient client;

    @BeforeEach
    void setUp() {
        client = new PDFServiceClient(restOperations, new ObjectMapper(), BASE_URL);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateFromHtmlPostsTemplateToPdfsEndpoint() {
        byte[] pdf = {1, 2, 3};
        when(restOperations.postForObject(eq(PDFS_ENDPOINT), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(pdf);

        assertThat(client.generateFromHtml(TEMPLATE, PLACEHOLDERS)).isEqualTo(pdf);

        ArgumentCaptor<HttpEntity<String>> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restOperations).postForObject(eq(PDFS_ENDPOINT), request.capture(), eq(byte[].class));
        assertThat(request.getValue().getHeaders().getContentType()).isEqualTo(PDFServiceClient.API_VERSION);
        assertThat(request.getValue().getHeaders().getAccept()).isEqualTo(List.of(MediaType.APPLICATION_PDF));
        assertThat(request.getValue().getBody())
            .contains("<html>{{ name }}</html>")
            .contains("John");
    }

    @Test
    void generateFromHtmlWrapsClientErrors() {
        when(restOperations.postForObject(eq(PDFS_ENDPOINT), any(HttpEntity.class), eq(byte[].class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> client.generateFromHtml(TEMPLATE, PLACEHOLDERS))
            .isInstanceOf(PDFServiceClientException.class)
            .hasMessage("Failed to request PDF from REST endpoint")
            .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void generateFromHtmlWrapsJsonSerialisationErrors() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("boom") { });
        PDFServiceClient failingClient = new PDFServiceClient(restOperations, failingMapper, BASE_URL);

        assertThatThrownBy(() -> failingClient.generateFromHtml(TEMPLATE, PLACEHOLDERS))
            .isInstanceOf(PDFServiceClientException.class)
            .hasMessage("Failed to convert PDF request into JSON");
    }

    @Test
    void generateFromHtmlRejectsEmptyTemplate() {
        assertThatThrownBy(() -> client.generateFromHtml(new byte[0], PLACEHOLDERS))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateFromHtmlRejectsNullPlaceholders() {
        assertThatThrownBy(() -> client.generateFromHtml(TEMPLATE, null))
            .isInstanceOf(NullPointerException.class);
    }
}
