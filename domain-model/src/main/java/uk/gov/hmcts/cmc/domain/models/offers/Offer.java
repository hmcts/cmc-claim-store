package uk.gov.hmcts.cmc.domain.models.offers;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.FutureDate;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.time.LocalDate;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class Offer {

    static final int CONTENT_LENGTH_LIMIT = 99000;

    @NotBlank
    @Size(max = CONTENT_LENGTH_LIMIT, message = "may not be longer than {max} characters")
    private final String content;

    @NotNull
    @FutureDate
    private final LocalDate completionDate;

    @Valid
    private final PaymentIntention paymentIntention;

    public Offer(String content, LocalDate completionDate, PaymentIntention paymentIntention) {
        this.content = content;
        this.completionDate = completionDate;
        this.paymentIntention = paymentIntention;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public Optional<PaymentIntention> getPaymentIntention() {
        return Optional.ofNullable(paymentIntention);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
