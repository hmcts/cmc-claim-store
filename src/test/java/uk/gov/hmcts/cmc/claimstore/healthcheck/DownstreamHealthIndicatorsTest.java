package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.PDFServiceProperties;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownstreamHealthIndicatorsTest {

    @Mock
    private PaymentsHealthApi paymentsHealthApi;

    @Mock
    private SendLetterHealthApi sendLetterHealthApi;

    @Mock
    private RestTemplate restTemplate;

    @Test
    void downstreamHealthIsReadFromHealthEndpointJson() throws Exception {
        DownstreamHealth health = new JacksonConfiguration().objectMapper()
            .readValue("{\"status\":\"UP\",\"components\":{\"db\":{\"status\":\"UP\"}}}", DownstreamHealth.class);

        assertThat(health.toHealth().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void paymentsReportsDownstreamStatus() {
        when(paymentsHealthApi.health()).thenReturn(new DownstreamHealth("DOWN"));

        assertThat(new PaymentsHealthIndicator(paymentsHealthApi).health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void paymentsIsDownWhenHealthCallFails() {
        when(paymentsHealthApi.health()).thenThrow(new IllegalStateException("unreachable"));

        Health health = new PaymentsHealthIndicator(paymentsHealthApi).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "java.lang.IllegalStateException: unreachable");
    }

    @Test
    void sendLetterReportsDownstreamStatus() {
        when(sendLetterHealthApi.health()).thenReturn(new DownstreamHealth("UP"));

        assertThat(new SendLetterHealthIndicator(sendLetterHealthApi).health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void sendLetterIsDownWhenHealthCallFails() {
        when(sendLetterHealthApi.health()).thenThrow(new IllegalStateException("unreachable"));

        assertThat(new SendLetterHealthIndicator(sendLetterHealthApi).health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void pdfServiceCallsHealthEndpointAndReportsStatus() {
        URI healthEndpoint = URI.create("http://pdf-service/health");
        when(restTemplate.exchange(eq(healthEndpoint), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(DownstreamHealth.class)))
            .thenReturn(ResponseEntity.ok(new DownstreamHealth("UP")));

        Health health = new PDFServiceHealthIndicator(restTemplate, pdfServiceProperties()).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        verify(restTemplate).exchange(eq(healthEndpoint), eq(HttpMethod.GET),
            eq(new HttpEntity<>("", acceptJson())), eq(DownstreamHealth.class));
    }

    @Test
    void pdfServiceIsDownWhenHealthCallFails() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(DownstreamHealth.class)))
            .thenThrow(new ResourceAccessException("timeout"));

        Health health = new PDFServiceHealthIndicator(restTemplate, pdfServiceProperties()).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    private static PDFServiceProperties pdfServiceProperties() {
        PDFServiceProperties properties = new PDFServiceProperties();
        properties.setUrl(URI.create("http://pdf-service"));
        return properties;
    }

    private static HttpHeaders acceptJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
