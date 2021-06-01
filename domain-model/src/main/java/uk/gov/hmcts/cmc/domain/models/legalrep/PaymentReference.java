package uk.gov.hmcts.cmc.domain.models.legalrep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentReference {
    private final String reference;
    private final String status;
    private final Integer errorCode;
    private final String errorCodeMessage;
    private final String dateCreated;

    public PaymentReference(String reference,
                            String status,
                            Integer errorCode,
                            String errorCodeMessage, String dateCreated) {
        this.reference = reference;
        this.status = status;
        this.errorCode = errorCode;
        this.errorCodeMessage = errorCodeMessage;
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
