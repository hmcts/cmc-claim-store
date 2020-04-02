package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ResponseAcceptation.class, name = "ACCEPTATION"),
    @JsonSubTypes.Type(value = ResponseRejection.class, name = "REJECTION")
})
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public abstract class ClaimantResponse {
    @NotNull
    protected final ClaimantResponseType type;

    @Min(value = 0)
    protected final BigDecimal amountPaid;

    protected final YesNoOption paymentReceived;

    protected final YesNoOption settleForAmount;

    public Optional<BigDecimal> getAmountPaid() {
        return Optional.ofNullable(amountPaid);
    }

    public Optional<YesNoOption> getPaymentReceived() {
        return Optional.ofNullable(paymentReceived);
    }

    public Optional<YesNoOption> getSettleForAmount() {
        return Optional.ofNullable(settleForAmount);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
