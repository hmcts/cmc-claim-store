package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ResponseAcceptation.class, name = "ACCEPTATION"),
    @JsonSubTypes.Type(value = ResponseRejection.class, name = "REJECTION")
})
@Getter
@EqualsAndHashCode
public abstract class ClaimantResponse {
    @NotNull
    private final ClaimantResponseType type;

    @Min(value = 0)
    private final BigDecimal amountPaid;

    public ClaimantResponse(ClaimantResponseType type, BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
        this.type = type;
    }

    public Optional<BigDecimal> getAmountPaid() {
        return Optional.ofNullable(amountPaid);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
