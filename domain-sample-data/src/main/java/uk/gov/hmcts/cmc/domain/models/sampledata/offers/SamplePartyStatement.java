package uk.gov.hmcts.cmc.domain.models.sampledata.offers;

import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement.PartyStatementBuilder;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

public class SamplePartyStatement {

    public static final PartyStatement offerPartyStatement = SamplePartyStatement.builder().build();

    public static final PartyStatement rejectPartyStatement = SamplePartyStatement.builder()
        .type(StatementType.REJECTION)
        .madeBy(MadeBy.CLAIMANT)
        .offer(null)
        .build();

    public static final PartyStatement acceptPartyStatement = SamplePartyStatement.builder()
        .type(StatementType.ACCEPTATION)
        .madeBy(MadeBy.CLAIMANT)
        .offer(null)
        .build();

    public static final PartyStatement counterSignPartyStatement = SamplePartyStatement.builder()
        .type(StatementType.COUNTERSIGNATURE)
        .madeBy(MadeBy.DEFENDANT)
        .offer(null)
        .build();

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
