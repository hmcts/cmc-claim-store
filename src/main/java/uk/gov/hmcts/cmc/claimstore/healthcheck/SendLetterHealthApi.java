package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "send-letter-health-api", url = "${send-letter.url}")
public interface SendLetterHealthApi {

    @GetMapping(value = "/health", headers = "Content-Type=application/json")
    DownstreamHealth health();
}
