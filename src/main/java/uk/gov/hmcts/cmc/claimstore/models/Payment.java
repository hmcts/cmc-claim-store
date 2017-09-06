package uk.gov.hmcts.cmc.claimstore.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.claimstore.utils.MonetaryConversions.penniesToPounds;

public class Payment {
    @NotBlank
    private final String id;
    /** The amount which was paid, in pennies. */
    @NotNull
    private final BigDecimal amount;
    @NotBlank
    private final String reference;
    @NotBlank
    private final String description;
    @NotBlank
    @JsonProperty("date_created")
    private final String dateCreated;
    @NotNull
    @Valid
    private final PaymentState state;

    public Payment(final String id,
                   final BigDecimal amount,
                   final String reference,
                   final String description,
                   final String dateCreated,
                   final PaymentState state) {
        this.id = id;
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.dateCreated = dateCreated;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @JsonIgnore
    public BigDecimal getAmountInPounds() {
        return penniesToPounds(amount);
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public PaymentState getState() {
        return state;
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final Payment payment = (Payment) other;
        return Objects.equals(id, payment.id)
            && Objects.equals(amount, payment.amount)
            && Objects.equals(reference, payment.reference)
            && Objects.equals(description, payment.description)
            && Objects.equals(dateCreated, payment.dateCreated)
            && Objects.equals(state, payment.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, reference, description, dateCreated, state);
    }
}
