package uk.gov.hmcts.cmc.ccd.domain.offers;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;

import java.time.LocalDate;

@Value
@Builder
public class CCDPartyStatement {
    private CCDStatementType type;
    private CCDMadeBy madeBy;
    private String offerContent;
    private LocalDate offerCompletionDate;
    private CCDPaymentIntention paymentIntention;
}
