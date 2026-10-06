package uk.gov.hmcts.cmc.claimstore.documents.pdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import uk.gov.hmcts.reform.pdf.service.client.GeneratePdfRequest;
import uk.gov.hmcts.reform.pdf.service.client.exception.PDFServiceClientException;
import uk.gov.hmcts.reform.pdf.service.client.util.Preconditions;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Same behaviour as pdf-service-client 8.0.2's PDFServiceClient, without its serviceHealthy() method.
 * That method returns a Spring Boot 3 type, which stops the library class being loaded reflectively
 * (by Spring or Mockito) on Spring Boot 4. Remove once pdf-service-client has a Boot 4 release.
 */
public class PDFServiceClient {

    public static final MediaType API_VERSION =
        MediaType.valueOf("application/vnd.uk.gov.hmcts.pdf-service.v2+json;charset=UTF-8");

    private final RestOperations restOperations;
    private final ObjectMapper objectMapper;
    private final URI htmlEndpoint;

    public PDFServiceClient(RestOperations restOperations, ObjectMapper objectMapper, URI pdfServiceBaseUrl) {
        this.restOperations = Objects.requireNonNull(restOperations);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.htmlEndpoint = Objects.requireNonNull(pdfServiceBaseUrl).resolve("/pdfs");
    }

    public byte[] generateFromHtml(byte[] template, Map<String, Object> placeholders) {
        Preconditions.requireNonEmpty(template);
        Objects.requireNonNull(placeholders);

        try {
            return restOperations.postForObject(htmlEndpoint, createRequestEntityFor(template, placeholders),
                byte[].class);
        } catch (HttpClientErrorException e) {
            throw new PDFServiceClientException("Failed to request PDF from REST endpoint", e);
        }
    }

    private HttpEntity<String> createRequestEntityFor(byte[] template, Map<String, Object> placeholders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(API_VERSION);
        headers.setAccept(List.of(MediaType.APPLICATION_PDF));

        GeneratePdfRequest request = new GeneratePdfRequest(new String(template), placeholders);
        try {
            return new HttpEntity<>(objectMapper.writeValueAsString(request), headers);
        } catch (JsonProcessingException e) {
            throw new PDFServiceClientException("Failed to convert PDF request into JSON", e);
        }
    }
}
