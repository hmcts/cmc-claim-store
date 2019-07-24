package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static org.assertj.core.api.Assertions.assertThat;

public class TheirDetailsHelperTest {

    @Test
    public void shouldReturnTrueWhenDefendantIsCompany() {
        TheirDetails defendant = SampleTheirDetails.builder().companyDetails();
        assertThat(TheirDetailsHelper.isDefendantBusiness(defendant)).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenDefendantIsOrganisation() {
        TheirDetails defendant = SampleTheirDetails.builder().organisationDetails();
        assertThat(TheirDetailsHelper.isDefendantBusiness(defendant)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenDefendantIsIndividual() {
        TheirDetails defendant = SampleTheirDetails.builder().individualDetails();
        assertThat(TheirDetailsHelper.isDefendantBusiness(defendant)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenDefendantIsSoleTrader() {
        TheirDetails defendant = SampleTheirDetails.builder().soleTraderDetails();
        assertThat(TheirDetailsHelper.isDefendantBusiness(defendant)).isFalse();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfDefendantIsNull() {
        assertThat(TheirDetailsHelper.isDefendantBusiness(null)).isFalse();
    }
}
