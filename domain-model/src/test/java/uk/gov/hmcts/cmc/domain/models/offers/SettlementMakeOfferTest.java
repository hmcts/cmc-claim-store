package uk.gov.hmcts.cmc.domain.models.offers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.exceptions.IllegalSettlementStatementException;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

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
        Offer offer = SampleOffer.builder().build();

        settlement.makeOffer(offer, MadeBy.DEFENDANT);

        List<PartyStatement> partyStatements = settlement.getPartyStatements();
        assertThat(partyStatements).hasSize(1);

        PartyStatement partyStatement = partyStatements.get(0);
        assertThat(partyStatement.getOffer().get()).isEqualTo(offer);
        assertThat(partyStatement.getType()).isEqualTo(StatementType.OFFER);
        assertThat(partyStatement.getMadeBy()).isEqualTo(MadeBy.DEFENDANT);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void shouldNotAllowDefendantToMakeTwoOffersInARow() {
        Offer offer = SampleOffer.builder().build();

        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.makeOffer(offer, MadeBy.DEFENDANT);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void shouldNotAllowClaimantToMakeTwoOffersInARow() {
        Offer offer = SampleOffer.builder().build();

        settlement.makeOffer(offer, MadeBy.CLAIMANT);
        settlement.makeOffer(offer, MadeBy.CLAIMANT);
    }

    @Test
    public void shouldAllowToMakeACounterOffers() {
        Offer offer = SampleOffer.builder().build();

        settlement.makeOffer(offer, MadeBy.CLAIMANT);
        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.makeOffer(offer, MadeBy.CLAIMANT);

        assertThat(settlement.getPartyStatements()).hasSize(3);
    }

}
