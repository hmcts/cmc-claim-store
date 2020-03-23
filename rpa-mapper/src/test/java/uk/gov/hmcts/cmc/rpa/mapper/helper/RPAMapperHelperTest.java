package uk.gov.hmcts.cmc.rpa.mapper.helper;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.time.LocalDate;
import javax.json.JsonObject;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class RPAMapperHelperTest {

    @Test
    public void shouldPrependWithTradingAs() {
        String soleTrader = "Charles & Sons";
        assertThat(RPAMapperHelper.prependWithTradingAs(soleTrader)).isEqualTo("T/A " + soleTrader);
    }

    @Test
    public void shouldReturnTrueWhenAddressIsAmended() {
        Address address = SampleAddress.builder().postcode("MK15 5EW").build();
        Party ownParty = SampleParty.builder().withAddress(address).individual();

        TheirDetails theirDetails = SampleTheirDetails.builder().individualDetails();

        assertThat(RPAMapperHelper.isAddressAmended(ownParty, theirDetails)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenAddressIsNotAmended() {
        Address address = SampleAddress.builder().postcode("MK15 5EW").build();
        Party ownParty = SampleParty.builder().withAddress(address).individual();

        TheirDetails theirDetails = SampleTheirDetails.builder().withAddress(address).individualDetails();

        assertThat(RPAMapperHelper.isAddressAmended(ownParty, theirDetails)).isFalse();
    }

    @Test
    public void shouldReturnJsonOfRepaymentPlan() {
        RepaymentPlan repaymentPlan = SampleRepaymentPlan.builder().build();
        JsonObject toJson = RPAMapperHelper.toJson(repaymentPlan);
        assertThat(toJson).isNotNull();
        assertThat(toJson).containsOnlyKeys("amount", "firstPayment", "frequency");
    }

    @Test
    public void shouldReturnClaimantPaidOnDateForStatesPaid() {
        Claim claim = SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation();
        assertThat(RPAMapperHelper.claimantPaidOnDate(claim))
            .isEqualTo((LocalDate.of(2016, 1, 2).toString()));
    }

    @Test
    public void shouldReturnClaimantPaidOnDateForPaidInFull() {
        LocalDate moneyReceivedOn = now();
        Claim claim = SampleClaim.builder().withMoneyReceivedOn(moneyReceivedOn).build();
        assertEquals(moneyReceivedOn, RPAMapperHelper.claimantPaidOnDate(claim));
    }
}
