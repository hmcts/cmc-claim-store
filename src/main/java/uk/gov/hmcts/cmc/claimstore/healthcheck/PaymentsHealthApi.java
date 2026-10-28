package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "payments-health-api", url = "${payments.api.url}")
public interface PaymentsHealthApi {

    @GetMapping("/health")
    DownstreamHealth health();
}
