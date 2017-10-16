package uk.gov.hmcts.cmc.claimstore.models.offers;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SettlementTest {

    @Test
    public void statusShouldBeUnsettledForNewInstance() {
        Settlement settlement = new Settlement();
        assertThat(settlement.getStatus()).isEqualByComparingTo(SettlementStatus.UNSETTLED);
    }

}
