package uk.gov.hmcts.cmc.ccd.domain.defendant;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.offers.CCDMadeBy;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.offers.CCDStatementType;

import java.time.LocalDate;

@Value
@Builder
public class CCDPartyStatement {
    private CCDStatementType type;
    private CCDMadeBy madeBy;
    private String offerContent;
    private LocalDate offerCompletionDate;
}
