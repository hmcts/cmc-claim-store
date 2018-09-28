package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.Valid;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ResponseAcceptation extends ClaimantResponse {

    @Valid
    private final CourtDetermination courtDetermination;

    @Valid
    private final PaymentIntention claimantPaymentIntention;

    private final FormaliseOption formaliseOption;

    private final DecisionType decisionType;

    @Builder
    @JsonCreator
    public ResponseAcceptation(
        BigDecimal amountPaid,
        CourtDetermination courtDetermination,
        PaymentIntention claimantPaymentIntention,
        FormaliseOption formaliseOption,
        DecisionType decisionType
    ) {
        super(amountPaid);
        this.courtDetermination = courtDetermination;
        this.claimantPaymentIntention = claimantPaymentIntention;
        this.formaliseOption = formaliseOption;
        this.decisionType = decisionType;
    }

    public Optional<CourtDetermination> getCourtDetermination() {
        return Optional.ofNullable(courtDetermination);
    }

    public Optional<PaymentIntention> getClaimantPaymentIntention() {
        return Optional.ofNullable(claimantPaymentIntention);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
