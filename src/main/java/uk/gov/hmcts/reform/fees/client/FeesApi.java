package uk.gov.hmcts.reform.fees.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.reform.fees.client.health.InternalHealth;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;

@FeignClient(name = "fees-api", url = "${fees.api.url}")
public interface FeesApi {
    @GetMapping("/health")
    InternalHealth health();

    @GetMapping("/fees-register/fees/lookup"
        + "?service={service}"
        + "&jurisdiction1={jurisdiction1}"
        + "&jurisdiction2={jurisdiction2}"
        + "&channel={channel}"
        + "&event={eventType}"
        + "&keyword={keyword}"
        + "&amount_or_volume={amount}"
    )
    FeeLookupResponseDto lookupFee(
        @PathVariable("service") String service,
        @PathVariable("jurisdiction1") String jurisdiction1,
        @PathVariable("jurisdiction2") String jurisdiction2,
        @PathVariable("channel") String channel,
        @PathVariable("eventType") String eventType,
        @PathVariable("keyword") String keyword,
        @PathVariable("amount") BigDecimal amount
    );

    @GetMapping("/fees-register/fees/lookup"
        + "?service={service}"
        + "&jurisdiction1={jurisdiction1}"
        + "&jurisdiction2={jurisdiction2}"
        + "&channel={channel}"
        + "&event={eventType}"
        + "&keyword={keyword}"
        + "&amount_or_volume={amount}"
    )
    FeeLookupResponseDto lookupFee(
        @PathVariable("service") String service,
        @PathVariable("jurisdiction1") String jurisdiction1,
        @PathVariable("jurisdiction2") String jurisdiction2,
        @PathVariable("channel") String channel,
        @PathVariable("eventType") String eventType,
        @PathVariable("amount") BigDecimal amount
    );
}
