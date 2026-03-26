package uk.gov.hmcts.cmc.claimstore.tests.idam;

import feign.Client;
import feign.Response;
import feign.hc5.ApacheHttp5Client;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
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
        public Client getFeignHttpClient() {
            return new ApacheHttp5Client(getHttpClient());
        }

        private CloseableHttpClient getHttpClient() {
            Timeout timeout = Timeout.ofMilliseconds(10000);
            RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setResponseTimeout(timeout)
                .build();

            return HttpClients.custom()
                .useSystemProperties()
                .disableRedirectHandling()
                .setDefaultRequestConfig(config)
                .build();
        }
    }
}
