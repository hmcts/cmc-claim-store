package uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPaymentIntention;

import java.math.BigDecimal;

@Builder
@Value
public class CCDResponseAcceptation {
    private BigDecimal amountPaid;

    private CCDCourtDetermination courtDetermination;

    private CCDPaymentIntention claimantPaymentIntention;

    private CCDFormaliseOption formaliseOption;
}
