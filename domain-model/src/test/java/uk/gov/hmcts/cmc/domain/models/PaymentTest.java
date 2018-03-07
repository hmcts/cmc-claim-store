package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePayment;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class PaymentTest {

    @Test
    public void shouldBeSuccessfulValidationForPayment() {
        //given
        Payment payment = SamplePayment.validDefaults();
        //when
        Set<String> errors = validate(payment);
        //then
        assertThat(errors).isNotNull().hasSize(0);
    }

    @Test
    public void shouldBeValidationMessageForInvalidPayment() {
        //given
        Payment payment = new Payment(null, null, null,
            null, null);
        //when
        Set<String> errors = validate(payment);
        //then
        assertThat(errors).hasSize(2).contains(
            "reference : may not be empty",
            "amount : may not be null"
        );
    }

}
