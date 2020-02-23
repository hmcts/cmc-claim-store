package uk.gov.hmcts.cmc.domain.models.ioc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailsResponse {

    String nextUrl;
    String reference;
    String status;
    BigDecimal amount;
}
