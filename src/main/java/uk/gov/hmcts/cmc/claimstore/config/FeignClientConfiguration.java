package uk.gov.hmcts.cmc.claimstore.config;

import feign.Client;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.netflix.feign.support.ResponseEntityDecoder;
import org.springframework.cloud.netflix.feign.support.SpringDecoder;
import org.springframework.cloud.netflix.feign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import uk.gov.hmcts.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.logging.httpcomponents.OutboundRequestIdSettingInterceptor;
import uk.gov.hmcts.reform.logging.httpcomponents.OutboundRequestLoggingInterceptor;

import java.util.Collection;

@Configuration
public class FeignClientConfiguration {

    @Autowired
    ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public Client getFeignHttpClient() {
        int timeout = 10000;
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .setSocketTimeout(timeout)
            .build();

        return new ApacheHttpClient(HttpClientBuilder
            .create()
            .useSystemProperties()
            .addInterceptorFirst(new OutboundRequestIdSettingInterceptor())
            .addInterceptorFirst((HttpRequestInterceptor) new OutboundRequestLoggingInterceptor())
            .addInterceptorLast((HttpResponseInterceptor) new OutboundRequestLoggingInterceptor())
            .setDefaultRequestConfig(config)
            .build());
    }

    @Bean
    @Primary
    @Scope("prototype")
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    @Bean
    @Primary
    @Scope("prototype")
    public Decoder decoder() {
        Decoder decoder = (response, type) -> {
            if (type instanceof Class && MultipartFile.class.isAssignableFrom((Class) type)) {
                Collection<String> contentTypes = response.headers().get("content-type");
                String contentType = "application/octet-stream";
                if (contentTypes.size() > 0) {
                    String[] temp = new String[contentTypes.size()];
                    contentTypes.toArray(temp);
                    contentType = temp[0];
                }


                byte[] bytes = StreamUtils.copyToByteArray(response.body().asInputStream());
                InMemoryMultipartFile inMemoryMultipartFile = new InMemoryMultipartFile("files","", contentType,bytes);
                return inMemoryMultipartFile;
            }
            return new SpringDecoder(messageConverters).decode(response, type);
        };
        return new ResponseEntityDecoder(decoder);
    }

    @Bean
    public feign.Logger.Level multipartLoggerLevel() {
        return feign.Logger.Level.FULL;
    }
}
