package uk.gov.hmcts.cmc.claimstore.tests.idam;

import feign.codec.Decoder;
import feign.codec.StringDecoder;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.feign.support.ResponseEntityDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@FeignClient(name = "idam-test-api", url = "${idam.api.url}/testing-support",
    configuration = IdamTestEmailApi.IdamConfiguration.class)
public interface IdamTestEmailApi {

    @RequestMapping(value = "/activationemail", method = GET)
    ResponseEntity<String> getActivationEmail();

    static class IdamConfiguration {
        @Bean
        @Primary
        Decoder feignDecoder() {
            return new ResponseEntityDecoder(new StringDecoder());
        }
    }
}
