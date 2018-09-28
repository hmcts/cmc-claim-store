package uk.gov.hmcts.cmc.domain.models.sampledata.offers;

import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement.PartyStatementBuilder;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

public class SamplePartyStatement {

    private SamplePartyStatement() {
        super();
    }

    public static PartyStatementBuilder builder() {
        return PartyStatement.builder()
            .type(StatementType.OFFER)
            .madeBy(MadeBy.DEFENDANT)
            .offer(SampleOffer.builder().build());
    }
}
