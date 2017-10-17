package uk.gov.hmcts.cmc.claimstore.models.offers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.offers.SampleOffer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SettlementMakeOfferTest {

    private Settlement settlement;

    @Before
    public void beforeEachTest() {
        settlement = new Settlement();
    }

    @Test
    public void shouldAllowDefendantToMakeAnInitialOffer() {
        Offer offer = SampleOffer.validDefaults();

        settlement.makeOffer(offer, MadeBy.defendant);

        List<PartyStatement> partyStatements = settlement.getPartyStatements();
        assertThat(partyStatements).hasSize(1);

        PartyStatement partyStatement = partyStatements.get(0);
        assertThat(partyStatement.getOffer().get()).isEqualTo(offer);
        assertThat(partyStatement.getType()).isEqualTo(StatementType.offer);
        assertThat(partyStatement.getMadeBy()).isEqualTo(MadeBy.defendant);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowDefendantToMakeTwoOffersInARow() {
        Offer offer = SampleOffer.validDefaults();

        settlement.makeOffer(offer, MadeBy.defendant);
        settlement.makeOffer(offer, MadeBy.defendant);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowClaimantToMakeTwoOffersInARow() {
        Offer offer = SampleOffer.validDefaults();

        settlement.makeOffer(offer, MadeBy.claimant);
        settlement.makeOffer(offer, MadeBy.claimant);
    }

    @Test
    public void shouldAllowToMakeACounterOffer() {
        Offer offer = SampleOffer.validDefaults();

        settlement.makeOffer(offer, MadeBy.claimant);
        settlement.makeOffer(offer, MadeBy.defendant);

        assertThat(settlement.getPartyStatements()).hasSize(2);
    }

}
