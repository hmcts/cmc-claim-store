package uk.gov.hmcts.cmc.claimstore.models.offers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SettlementTest {

    @Mock
    private PartyStatement partyStatement;

    @Test
    public void statusShouldBeUnsettledForNewInstance() {
        Settlement settlement = new Settlement();
        assertThat(settlement.getStatus()).isEqualByComparingTo(SettlementStatus.unsettled);
    }

    @Test
    public void partyStatementsShouldBeAnEmptyListForNewInstance() {
        Settlement settlement = new Settlement();
        assertThat(settlement.getPartyStatements()).isEmpty();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnedPartyStatementsShouldBeAnUnmodifiableList() {
        List<PartyStatement> statements = new Settlement().getPartyStatements();
        statements.add(partyStatement);
    }

}
