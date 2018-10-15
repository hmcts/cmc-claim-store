package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class PaymentDeclaration {

    @NotNull
    @DateNotInTheFuture
    private final LocalDate paidDate;

    @NotBlank
    @Size(max = 99000)
    private final String explanation;

    public PaymentDeclaration(LocalDate paidDate, String explanation) {
        this.paidDate = paidDate;
        this.explanation = explanation;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
