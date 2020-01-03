package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
@GroupSequence({
    PaymentIntention.Responding.class,
    CourtDetermination.class
})
public class CourtDetermination {

    @NotNull
    @Valid
    private final PaymentIntention courtDecision;

    @Valid
    @NotNull
    private final PaymentIntention courtPaymentIntention;

    private final String rejectionReason;

    @NotNull
    private final BigDecimal disposableIncome;

    @NotNull
    private final DecisionType decisionType;

    @Builder
    public CourtDetermination(
        PaymentIntention courtDecision,
        PaymentIntention courtPaymentIntention,
        String rejectionReason,
        BigDecimal disposableIncome,
        DecisionType decisionType
    ) {
        this.courtDecision = courtDecision;
        this.courtPaymentIntention = courtPaymentIntention;
        this.rejectionReason = rejectionReason;
        this.disposableIncome = disposableIncome;
        this.decisionType = decisionType;
    }

    public Optional<String> getRejectionReason() {
        return Optional.ofNullable(rejectionReason);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
