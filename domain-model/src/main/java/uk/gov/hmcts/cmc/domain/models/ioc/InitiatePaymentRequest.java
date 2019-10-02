package uk.gov.hmcts.cmc.domain.models.ioc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;

import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiatePaymentRequest {
    @Valid
    @NotNull
    private final UUID externalId;

    @Valid
    @NotNull
    private final Amount amount;

    @Valid
    private final Interest interest;
}
