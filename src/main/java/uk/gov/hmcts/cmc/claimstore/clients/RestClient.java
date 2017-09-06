package uk.gov.hmcts.cmc.claimstore.clients;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.reform.logging.httpcomponents.OutboundRequestIdSettingInterceptor;
import uk.gov.hmcts.reform.logging.httpcomponents.OutboundRequestLoggingInterceptor;

import java.util.HashMap;

/**
 * Used as a base for all HTTP clients.
 */
public class RestClient {

    private final String apiUrl;
    private final RestTemplate rest;
    private final HttpHeaders headers;
    private JsonMapper jsonMapper;

    public RestClient(final RestTemplate rest,
                      final String apiUrl,
                      final HttpHeaders headers,
                      final JsonMapper jsonMapper) {
        this.rest = rest;
        this.apiUrl = apiUrl;
        this.headers = headers;
        this.jsonMapper = jsonMapper;
    }

    public <T> T get(final String path, Class<T> responseClass) {
        return rest.getForObject(apiUrl + path, responseClass, new HashMap<>());
    }

    public <T> T get(String path, String authorisation, Class<T> responseClass) {
        HttpEntity<String> requestEntity = new HttpEntity<>(
            prepareHttpHeadersWithAuthorisation(authorisation)
        );
        return rest.exchange(
            apiUrl + path, HttpMethod.GET, requestEntity, responseClass
        ).getBody();
    }

    public <T> ResponseEntity<T> post(
        final String path, final Object requestBody, final String authorisation, Class<T> responseClass) {

        HttpEntity<String> requestEntity = new HttpEntity<>(
            jsonMapper.toJson(requestBody),
            prepareHttpHeadersWithAuthorisation(authorisation)
        );

        return rest.exchange(
            apiUrl + path, HttpMethod.POST, requestEntity, responseClass
        );
    }

    private MultiValueMap<String, String> prepareHttpHeadersWithAuthorisation(final String authorisation) {
        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>(headers);
        requestHeaders.add(HttpHeaders.AUTHORIZATION, authorisation);

        return requestHeaders;
    }

    public static class Builder {
        public static final String DEFAULT_ACCEPT = MediaType.ALL_VALUE;
        public static final String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON_UTF8_VALUE;

        private RestTemplate restOperations;
        private HttpHeaders headers;
        private String apiDomain;
        private JsonMapper jsonMapper;

        private Builder() {
            this.restOperations = new RestTemplate(getClientHttpRequestFactory());
            this.headers = createDefaultHeaders();
        }

        private ClientHttpRequestFactory getClientHttpRequestFactory() {
            int timeout = 10000;
            RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
            CloseableHttpClient client = HttpClientBuilder
                .create()
                .useSystemProperties()
                .addInterceptorFirst(new OutboundRequestIdSettingInterceptor())
                .addInterceptorFirst((HttpRequestInterceptor) new OutboundRequestLoggingInterceptor())
                .addInterceptorLast((HttpResponseInterceptor) new OutboundRequestLoggingInterceptor())
                .setDefaultRequestConfig(config)
                .build();
            return new HttpComponentsClientHttpRequestFactory(client);
        }

        public static Builder of() {
            return new Builder();
        }

        public static HttpHeaders createDefaultHeaders() {
            HttpHeaders defaultHeaders = new HttpHeaders();
            defaultHeaders.add(HttpHeaders.CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            defaultHeaders.add(HttpHeaders.ACCEPT, DEFAULT_ACCEPT);

            return defaultHeaders;
        }

        public Builder restTemplate(final RestTemplate rest) {
            this.restOperations = rest;

            return this;
        }

        public Builder apiDomain(final String domain) {
            this.apiDomain = domain;

            return this;
        }

        public Builder accept(final String value) {
            return setHeader(HttpHeaders.ACCEPT, value);
        }

        public Builder contentType(final String value) {
            return setHeader(HttpHeaders.CONTENT_TYPE, value);
        }

        public Builder jsonMapper(final JsonMapper jsonMapper) {
            this.jsonMapper = jsonMapper;

            return this;
        }

        public RestClient build() {
            return new RestClient(restOperations, apiDomain, headers, jsonMapper);
        }

        private Builder setHeader(final String header, final String value) {
            if (headers.containsKey(header)) {
                headers.remove(header);
            }
            headers.add(header, value);

            return this;
        }
    }
}
