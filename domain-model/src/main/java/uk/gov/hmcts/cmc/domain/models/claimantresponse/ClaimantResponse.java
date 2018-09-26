package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ResponseAcceptation.class, name = "acceptation"),
    @JsonSubTypes.Type(value = ResponseRejection.class, name = "rejection")
})
@EqualsAndHashCode
public abstract class ClaimantResponse {

    private final BigDecimal amountPaid;

    public ClaimantResponse(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Optional<BigDecimal> getAmountPaid() {
        return Optional.ofNullable(amountPaid);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
