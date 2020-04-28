package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Payment {
    private final String id;
    /**
     * The amount which was paid, in pennies for payments v1 or pounds with payments v2.
     */
    @NotNull
    private final BigDecimal amount;
    @NotBlank
    private final String reference;
    private final String dateCreated;
    private final PaymentStatus status;
    // Add not blank after we switch to IOC
    private final String nextUrl;

    public Payment(
        String id,
        BigDecimal amount,
        String reference,
        String dateCreated,
        PaymentStatus status,
        String nextUrl
    ) {
        this.id = id;
        this.amount = amount;
        this.reference = reference;
        this.dateCreated = dateCreated;
        this.status = status;
        this.nextUrl = nextUrl;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
