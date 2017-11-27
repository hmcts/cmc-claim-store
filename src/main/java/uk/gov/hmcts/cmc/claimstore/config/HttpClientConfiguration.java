package uk.gov.hmcts.cmc.claimstore.config;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.logging.httpcomponents.OutboundRequestIdSettingInterceptor;
import uk.gov.hmcts.reform.logging.httpcomponents.OutboundRequestLoggingInterceptor;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public Client getFeignHttpClient() {
        return new ApacheHttpClient(getApacheHttpClient());
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        final CloseableHttpClient client = getApacheHttpClient();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
        return restTemplate;
    }

    private CloseableHttpClient getApacheHttpClient() {
        int timeout = 10000;
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .setSocketTimeout(timeout)
            .build();

        return HttpClientBuilder
            .create()
            .useSystemProperties()
            .addInterceptorFirst(new OutboundRequestIdSettingInterceptor())
            .addInterceptorFirst((HttpRequestInterceptor) new OutboundRequestLoggingInterceptor())
            .addInterceptorLast((HttpResponseInterceptor) new OutboundRequestLoggingInterceptor())
            .setDefaultRequestConfig(config)
            .build();
    }

}
