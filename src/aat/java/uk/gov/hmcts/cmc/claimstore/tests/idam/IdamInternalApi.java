package uk.gov.hmcts.cmc.claimstore.tests.idam;

import feign.Client;
import feign.Logger;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "idam-internal-api",
    url = "${idam.api.url}",
    configuration = IdamInternalApi.Configuration.class
)
public interface IdamInternalApi {

    @RequestMapping(
        method = RequestMethod.GET,
        value = "/pin",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Response authenticatePinUser(
        @RequestHeader("pin") final String pin,
        @RequestParam("client_id") final String clientId,
        @RequestParam("redirect_uri") final String redirectUri
    );

    @RequestMapping(method = RequestMethod.POST,
        value = "/login/uplift",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Response upliftUser(
        @RequestParam("userName") String username,
        @RequestParam("password") String password,
        @RequestParam("jwt") String pinUserAuthorisation,
        @RequestParam("clientId") final String clientId,
        @RequestParam("redirectUri") final String redirectUri
    );

    class Configuration {
        @Bean
        Logger.Level feignLoggerLevel() {
            return Logger.Level.FULL;
        }

        @Bean
        public Client getFeignHttpClient() {
            return new ApacheHttpClient(getHttpClient());
        }

        private CloseableHttpClient getHttpClient() {
            int timeout = 60000;
            RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

            return HttpClientBuilder
                .create()
                .useSystemProperties()
                .disableRedirectHandling()
                .setDefaultRequestConfig(config)
                .build();
        }
    }
}
