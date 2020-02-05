package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentStatusTest {

    @Test
    public void shouldMapToPaymentStatusFromString() {
        assertThat(PaymentStatus.fromValue("Initiated")).isEqualTo(PaymentStatus.INITIATED);
        assertThat(PaymentStatus.fromValue("initiated")).isEqualTo(PaymentStatus.INITIATED);
        assertThat(PaymentStatus.fromValue("Success")).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(PaymentStatus.fromValue("Failed")).isEqualTo(PaymentStatus.FAILED);
        assertThat(PaymentStatus.fromValue("failed")).isEqualTo(PaymentStatus.FAILED);
        assertThat(PaymentStatus.fromValue("Pending")).isEqualTo(PaymentStatus.PENDING);
        assertThat(PaymentStatus.fromValue("pending")).isEqualTo(PaymentStatus.PENDING);
        assertThat(PaymentStatus.fromValue("Declined")).isEqualTo(PaymentStatus.DECLINED);
        assertThat(PaymentStatus.fromValue("declined")).isEqualTo(PaymentStatus.DECLINED);
    }

}
