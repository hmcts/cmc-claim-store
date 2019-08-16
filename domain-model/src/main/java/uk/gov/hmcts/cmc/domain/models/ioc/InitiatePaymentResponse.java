package uk.gov.hmcts.cmc.domain.models.ioc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class InitiatePaymentResponse {

    String nextUrl;
}
