package uk.gov.hmcts.document.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.document.exception.TemporaryStoreFailureException;

import java.io.IOException;
import java.util.List;

final class UploadRequestBuilder {

    private UploadRequestBuilder() {
        throw new UnsupportedOperationException();
    }

    static MultiValueMap<String, Object> prepareRequest(List<MultipartFile> files) {
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        files.stream()
            .map(UploadRequestBuilder::buildPartFromFile)
            .forEach(file -> parameters.add("files", file));
        parameters.add("classification", "RESTRICTED");
        return parameters;
    }

    private static HttpEntity<Resource> buildPartFromFile(MultipartFile file) {
        return new HttpEntity<>(buildByteArrayResource(file), buildPartHeaders(file));
    }

    private static HttpHeaders buildPartHeaders(MultipartFile file) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(buildMediaType(file.getContentType()));
        return headers;
    }

    private static MediaType buildMediaType(String contentType) {
        final String[] splitz = contentType.split("/");
        if (splitz.length > 1) {
            return new MediaType(splitz[0], splitz[1]);
        }
        return new MediaType(splitz[0]);
    }

    private static ByteArrayResource buildByteArrayResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException ioException) {
            throw new TemporaryStoreFailureException(ioException);
        }
    }

}
