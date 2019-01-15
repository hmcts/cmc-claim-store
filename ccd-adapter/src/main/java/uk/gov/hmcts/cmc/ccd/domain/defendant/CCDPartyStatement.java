package uk.gov.hmcts.cmc.ccd.domain.defendant;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDStatementType;

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
