package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePayment;
import uk.gov.hmcts.cmc.domain.utils.BeanValidator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentTest {

    @Test
    public void shouldBeSuccessfulValidationForPayment() {
        //given
        Payment payment = SamplePayment.validDefaults();
        //when
        Set<String> errors = BeanValidator.validate(payment);
        //then
        assertThat(errors).isNotNull().hasSize(0);
    }

    @Test
    public void shouldBeValidationMessageForInvalidPayment() {
        //given
        Payment payment = new Payment(null, null, null,
            null, null, null);
        //when
        Set<String> errors = BeanValidator.validate(payment);
        //then
        assertThat(errors).hasSize(6).contains(
            "id : may not be empty",
            "reference : may not be empty",
            "description : may not be empty",
            "dateCreated : may not be empty",
            "amount : may not be null",
            "state : may not be null"
        );
    }

}
