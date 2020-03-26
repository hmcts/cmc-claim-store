package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

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

    @Builder(toBuilder = true)
    @JsonCreator
    public ResponseAcceptation(
        BigDecimal amountPaid,
        YesNoOption paymentReceived,
        YesNoOption settleForAmount,
        CourtDetermination courtDetermination,
        PaymentIntention claimantPaymentIntention,
        FormaliseOption formaliseOption
    ) {
        super(ClaimantResponseType.ACCEPTATION, amountPaid, paymentReceived, settleForAmount);
        this.courtDetermination = courtDetermination;
        this.claimantPaymentIntention = claimantPaymentIntention;
        this.formaliseOption = formaliseOption;
    }

    public Optional<CourtDetermination> getCourtDetermination() {
        return Optional.ofNullable(courtDetermination);
    }

    public Optional<PaymentIntention> getClaimantPaymentIntention() {
        return Optional.ofNullable(claimantPaymentIntention);
    }

    public Optional<FormaliseOption> getFormaliseOption() {
        return Optional.ofNullable(formaliseOption);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
