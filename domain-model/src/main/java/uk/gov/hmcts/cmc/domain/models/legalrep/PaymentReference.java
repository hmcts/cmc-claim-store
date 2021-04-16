package uk.gov.hmcts.cmc.domain.models.legalrep;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@Data
@EqualsAndHashCode
public class PaymentReference {
    private final String reference;
    private final String status;
    private final String errorCode;
    private final String errorMessage;

    public PaymentReference(String reference, String status, String errorCode, String errorMessage) {
        this.reference = reference;
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }


}
