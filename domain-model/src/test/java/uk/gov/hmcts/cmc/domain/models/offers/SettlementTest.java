package uk.gov.hmcts.cmc.domain.models.offers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.exceptions.IllegalSettlementStatementException;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SettlementTest {

    private static final String COUNTER_OFFER = "Get me a new roof instead";
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
            .withContent(COUNTER_OFFER)
            .build();

        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.makeOffer(counterOffer, MadeBy.CLAIMANT);

        assertThat(settlement.getLastStatement().getOffer().get()).isEqualTo(counterOffer);
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
    public void partyIsNotAllowedToAcceptOfferWhenOfferWasAlreadyAccepted() {
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
    public void partyIsNotAllowedToRejectOfferWhenOfferWasAlreadyRejected() {
        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.reject(MadeBy.CLAIMANT);

        settlement.reject(MadeBy.CLAIMANT);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void partyIsNotAllowedToRejectOfferWhenTheOnlyOfferWasMadeByThemselves() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT);
        settlement.reject(MadeBy.CLAIMANT);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void getLastStatementShouldThrowIllegalStateWhenNoStatementsHaveBeenMade() {
        settlement.getLastStatement();
    }

    @Test
    public void getLastOfferStatementShouldGiveLastStatementThatIsAnOffer() {
        settlement.makeOffer(offer, MadeBy.CLAIMANT);
        settlement.reject(MadeBy.DEFENDANT);

        Offer counterOffer = SampleOffer.builder()
            .withContent(COUNTER_OFFER)
            .build();

        settlement.makeOffer(counterOffer, MadeBy.DEFENDANT);
        settlement.accept(MadeBy.CLAIMANT);

        assertThat(settlement.getLastOfferStatement().getOffer()
            .orElseThrow(IllegalArgumentException::new)
            .getContent()
        )
            .isEqualTo(COUNTER_OFFER);
    }

    @Test(expected = IllegalSettlementStatementException.class)
    public void getLastOfferStatementShouldThrowWhenNoStatements() {
        settlement.getLastOfferStatement();
    }


}
