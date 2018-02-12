package uk.gov.hmcts.cmc.domain.models.sampledata.offers;

import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

public class SamplePartyStatement {

    private StatementType statementType = StatementType.OFFER;
    private MadeBy madeBy = MadeBy.CLAIMANT;
    private Offer offer = SampleOffer.validDefaults();

    public static PartyStatement validDefaults() {
        return builder().build();
    }

    public static SamplePartyStatement builder() {
        return new SamplePartyStatement();
    }

    public PartyStatement build() {
        return offer != null
            ? new PartyStatement(statementType, madeBy, offer)
            : new PartyStatement(statementType, madeBy);
    }

    public SamplePartyStatement withStatementType(StatementType statementType) {
        this.statementType = statementType;
        return this;
    }

    public SamplePartyStatement withMadeBy(MadeBy madeBy) {
        this.madeBy = madeBy;
        return this;
    }

    public SamplePartyStatement withOffer(Offer offer) {
        this.offer = offer;
        return this;
    }
}
