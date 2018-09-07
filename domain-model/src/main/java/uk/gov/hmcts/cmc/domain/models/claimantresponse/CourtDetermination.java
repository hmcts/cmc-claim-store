package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class CourtDetermination {

    @NotNull
    @Valid
    private final PaymentIntention courtCalculatedPaymentIntention;
    
    private final String rejectionReason;

    public CourtDetermination(PaymentIntention courtCalculatedPaymentIntention, String rejectionReason) {
        this.courtCalculatedPaymentIntention = courtCalculatedPaymentIntention;
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
