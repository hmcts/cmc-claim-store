package uk.gov.hmcts.cmc.claimstore.models.offers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.offers.SampleOffer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SettlementTest {

    @Mock
    private PartyStatement partyStatement;

    private Settlement settlement;

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
        Offer offer = SampleOffer.validDefaults();
        Offer counterOffer = SampleOffer.builder()
            .withContent("Get me a new roof instead")
            .build();

        settlement.makeOffer(offer, MadeBy.DEFENDANT);
        settlement.makeOffer(counterOffer, MadeBy.CLAIMANT);

        assertThat(settlement.getLastStatement().getOffer().get()).isEqualTo(counterOffer);
    }

    @Test(expected = IllegalStateException.class)
    public void getLastStatementShouldThrowIllegalStateWhenNoStatementsHaveBeenMade() {
        settlement.getLastStatement();
    }

}
