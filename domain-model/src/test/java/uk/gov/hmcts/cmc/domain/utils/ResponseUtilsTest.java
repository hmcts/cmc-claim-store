package uk.gov.hmcts.cmc.domain.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseUtilsTest {

    @Test
    public void isResponseStatesPaidOnFullDefenceAlreadyPaidResponseShouldBeTrue() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.ALREADY_PAID).build();

        assertThat(ResponseUtils.isResponseStatesPaid(response)).isTrue();
    }

    @Test
    public void isResponseStatesPaidOnPartAdmissionWithPaymentDeclarationShouldBeTrue() {
        Response response = PartAdmissionResponse.builder()
            .paymentDeclaration(SamplePaymentDeclaration.builder().build()).build();

        assertThat(ResponseUtils.isResponseStatesPaid(response)).isTrue();
    }

    @Test
    public void isResponseStatesPaidOnNullResponseShouldBeFalse() {
        assertThat(ResponseUtils.isResponseStatesPaid(null)).isFalse();
    }

    @Test
    public void isResponseStatesPaidOnFullAdmissionShouldBeFalse() {
        Response response = SampleResponse.FullAdmission.builder().build();

        assertThat(ResponseUtils.isResponseStatesPaid(response)).isFalse();
    }

    @Test
    public void isResponseStatesPaidOnFullDefenseWithDisputeShouldBeFalse() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.DISPUTE).build();

        assertThat(ResponseUtils.isResponseStatesPaid(response)).isFalse();
    }

    @Test
    public void isResponseStatesPaidOnPartAdmissionWithNoPaymentDeclarationShouldBeFalse() {
        Response response = PartAdmissionResponse.builder().paymentDeclaration(null).build();

        assertThat(ResponseUtils.isResponseStatesPaid(response)).isFalse();
    }
}
