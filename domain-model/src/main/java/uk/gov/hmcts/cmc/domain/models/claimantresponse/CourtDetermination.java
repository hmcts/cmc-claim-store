package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class CourtDetermination {

    @NotNull
    @Valid
    private final PaymentIntention courtDecision;

    private final String rejectionReason;

    @NotNull
    private final BigDecimal disposableIncome;

    @Builder
    public CourtDetermination(PaymentIntention courtDecision,
                              String rejectionReason,
                              BigDecimal disposableIncome
    ) {
        this.courtDecision = courtDecision;
        this.rejectionReason = rejectionReason;
        this.disposableIncome = disposableIncome;
    }

    public Optional<String> getRejectionReason() {
        return Optional.ofNullable(rejectionReason);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
