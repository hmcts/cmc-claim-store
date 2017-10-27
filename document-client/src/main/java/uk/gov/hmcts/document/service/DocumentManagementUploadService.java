package uk.gov.hmcts.document.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.document.model.FileUploadResponse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.StreamSupport.stream;
import static uk.gov.hmcts.document.service.UploadRequestBuilder.prepareRequest;

@Service
public class DocumentManagementUploadService {

    private static final Logger log = LoggerFactory.getLogger(DocumentManagementUploadService.class);

    @Value("${document.management.upload.file.url}")
    private String documentManagementServiceURL;

    @Autowired
    private RestTemplate template;


    @CircuitBreaker(exclude = HttpClientErrorException.class)
    public List<FileUploadResponse> uploadFiles(List<MultipartFile> files, String authorizationToken, String requestId) {
        MultiValueMap<String, Object> parameters = prepareRequest(files);

        HttpHeaders httpHeaders = setHttpHeaders(authorizationToken);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parameters, httpHeaders);

        final Object jsonNodes = template.postForObject(documentManagementServiceURL, httpEntity, Object.class);

        JsonNode filesJsonArray = ((JsonNode) jsonNodes)
            .get("_embedded")
            .get("documents");

        log.info("For Request Id {} : File upload response from Evidence Management service is {}", requestId,
            filesJsonArray);

        return prepareUploadResponse(filesJsonArray);
    }

    @Recover
    public List<FileUploadResponse> serviceUnavailable(List<MultipartFile> files, String authorizationToken,
                                                       String requestId) {
        log.info("Request Id {} failed as the EM Service is unavailable", requestId);

        return asList(new FileUploadResponse(HttpStatus.SERVICE_UNAVAILABLE));
    }

    private List<FileUploadResponse> prepareUploadResponse(JsonNode filesJsonArray) {
        Stream<JsonNode> filesStream = stream(filesJsonArray.spliterator(), false);

        return filesStream
            .map(this::createUploadResponse)
            .collect(Collectors.toList());
    }

    private FileUploadResponse createUploadResponse(JsonNode storedFile) {
        FileUploadResponse fileUploadResponse = new FileUploadResponse(HttpStatus.OK,
            new HalLinkDiscoverer().findLinkWithRel("self", storedFile.toString()).getHref(),
            storedFile.get("originalDocumentName").asText(),
            storedFile.get("mimeType").asText(),
            storedFile.get("createdBy").asText(),
            storedFile.get("createdOn").asText(),
            storedFile.get("lastModifiedBy").asText(),
            storedFile.get("modifiedOn").asText()
        );

        return fileUploadResponse;
    }

    public HttpHeaders setHttpHeaders(String authorizationToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authorizationToken);
        headers.set("Content-Type", "multipart/form-data");
        return headers;
    }
}
