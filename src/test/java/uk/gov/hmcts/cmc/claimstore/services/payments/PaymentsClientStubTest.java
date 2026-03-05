package uk.gov.hmcts.cmc.claimstore.services.payments;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentsClientStubTest {

    private final PaymentsClientStub paymentsClientStub = new PaymentsClientStub();

    @Test
    void shouldReturnSyntheticPaymentWhenReferenceDoesNotExist() {
        String missingReference = "RC-missing-reference";

        PaymentDto payment = paymentsClientStub.retrieveCardPayment("auth", missingReference);

        assertThat(payment.getReference()).isEqualTo(missingReference);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS.getStatus());
        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldCacheSyntheticPaymentForSubsequentRetrieval() {
        String missingReference = "RC-repeat-reference";

        PaymentDto first = paymentsClientStub.retrieveCardPayment("auth", missingReference);
        PaymentDto second = paymentsClientStub.retrieveCardPayment("auth", missingReference);

        assertThat(second).isSameAs(first);
    }
}
