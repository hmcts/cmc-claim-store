package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class ResponseAcceptation extends ClaimantResponse {

    @Valid
    private final CourtDetermination courtDetermination;

    @Valid
    private final PaymentIntention claimantPaymentIntention;

    @NotNull
    private final FormaliseOption formaliseOption;

    @Builder
    @JsonCreator
    public ResponseAcceptation(
        BigDecimal amountPaid,
        CourtDetermination courtDetermination,
        PaymentIntention claimantPaymentIntention,
        FormaliseOption formaliseOption
    ) {
        super(amountPaid);
        this.courtDetermination = courtDetermination;
        this.claimantPaymentIntention = claimantPaymentIntention;
        this.formaliseOption = formaliseOption;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
