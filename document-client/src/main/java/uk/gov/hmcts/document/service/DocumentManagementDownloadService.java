package uk.gov.hmcts.document.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.document.exception.BinaryUrlNotAvailableException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import static org.springframework.util.StreamUtils.copyToByteArray;
import static org.springframework.util.StringUtils.isEmpty;

@Service
public class DocumentManagementDownloadService {

    private static final Logger log = LoggerFactory.getLogger(DocumentManagementDownloadService.class);
    private final static MediaType MEDIA_TYPE =
        new MediaType("application", "vnd.uk.gov.hmcts.dm.document.v1+json");

    @Value("${document.management.upload.file.url}")
    private String evidenceManagementServiceURL;

    @Autowired
    private RestTemplate template;

    @CircuitBreaker(exclude = HttpClientErrorException.class)
    public ResponseEntity<InputStreamResource> downloadFile(String fileUrl, String authorizationToken, String requestId)
        throws IOException {

        HttpHeaders httpHeaders = setHttpHeaders(authorizationToken);

        log.info("RequestId : {} and Downloading file for self url {} ", requestId, fileUrl);

        String binaryFileUrl = binaryUrl(fileUrl, httpHeaders);

        log.info("RequestId : {} and Binary file url retrieved is {} ", requestId, binaryFileUrl);

        if (isEmpty(binaryFileUrl)) {
            throw new BinaryUrlNotAvailableException("Binary url for that resource is not present");
        }

        ResponseEntity<Resource> resource = template.exchange(binaryFileUrl, HttpMethod.GET,
            new HttpEntity<>(httpHeaders), Resource.class);

        return ResponseEntity.status(HttpStatus.OK)
            .headers(resource.getHeaders())
            .body(new InputStreamResource(
                new ByteArrayInputStream(copyToByteArray(resource.getBody().getInputStream()))));
    }

    @Recover
    public ResponseEntity<InputStreamResource> serviceUnavailable(String fileUrl, String authorizationToken,
                                                                  String requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        return new ResponseEntity<>(headers, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public String binaryUrl(String url, HttpHeaders httpHeaders) {
        Traverson traverson = initialiseTraverson(url);

        return traverson
            .follow("$._links.binary.href")
            .withHeaders(httpHeaders)
            .asLink()
            .getHref();
    }

    Traverson initialiseTraverson(String url) {
        return new Traverson(URI.create(url), MEDIA_TYPE);
    }

    public HttpHeaders setHttpHeaders(String authorizationToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authorizationToken);
        headers.set("Content-Type", MEDIA_TYPE.toString());
        return headers;
    }
}
