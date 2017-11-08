package uk.gov.hmcts.cmc.claimstore.models.offers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.IllegalSettlementStatementException;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.offers.SampleOffer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SettlementTest {

    @Mock
    private PartyStatement partyStatement;

    private Settlement settlement;
    private static final Offer offer = SampleOffer.validDefaults();

    @Before
    public void beforeEachTest() {
        settlement = new Settlement();
    }

    @Test
    public void partyStatementsShouldBeAnEmptyListForNewInstance() {
        assertThat(settlement.getPartyStatements()).isEmpty();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnedPartyStatementsShouldBeAnUnmodifiableList() {
        List<PartyStatement> statements = settlement.getPartyStatements();
        statements.add(partyStatement);
    }

    @Test
    public void getLastStatementShouldReturnLastStatement() {
        Offer counterOffer = SampleOffer.builder()
            .withContent("Get me a new roof instead")
            .build();

        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.makeOffer(counterOffer, MadeBy.CLAIMANT);

        assertThat(settlement.getLastStatement().getOffer().get()).isEqualTo(counterOffer);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void theSamePartyIsNotAllowedToMakeTwoOffersInRow() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.makeOffer(offer, MadeBy.DEFENDANT);
    }

    @Test
    public void claimantCanAcceptAnOffer() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.accept(MadeBy.CLAIMANT);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.ACCEPTATION);
    }

    @Test
    public void defendantCanAcceptAnOffer() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT);
        settlement.accept(MadeBy.DEFENDANT);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.ACCEPTATION);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToAcceptOfferWhenOfferWasNotMade() {
        settlement.accept(MadeBy.CLAIMANT);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToAcceptOfferWhenOfferWasAlreadyAcceptedByThemselves() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.accept(MadeBy.CLAIMANT);

        settlement.accept(MadeBy.CLAIMANT);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToAcceptOfferWhenTheOnlyOfferWasMadeByThemselves() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT);
        settlement.accept(MadeBy.CLAIMANT);
    }

    @Test
    public void claimantCanRejectAnOffer() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.reject(MadeBy.CLAIMANT);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.REJECTION);
    }

    @Test
    public void defendantCanRejectAnOffer() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT);
        settlement.reject(MadeBy.DEFENDANT);

        assertThat(settlement.getLastStatement().getType()).isEqualTo(StatementType.REJECTION);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToRejectOfferWhenOfferWasNotMade() {
        settlement.reject(MadeBy.CLAIMANT);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToRejectOfferWhenOfferWasAlreadyRejectedByThemselves() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.reject(MadeBy.CLAIMANT);

        settlement.reject(MadeBy.CLAIMANT);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToRejectOfferWhenTheOnlyOfferWasMadeByThemselves() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT);
        settlement.reject(MadeBy.CLAIMANT);
    }

    @Test(expected = IllegalStateException.class)
    public void getLastStatementShouldThrowIllegalStateWhenNoStatementsHaveBeenMade() {
        settlement.getLastStatement();
    }

}
