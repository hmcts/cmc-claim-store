package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePaymentDeclaration;

import java.time.LocalDate;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class PaymentDeclarationTest {

    @Test
    public void shouldHaveNoValidationMessageWhenInstanceIsValid() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.validDefaults();
        //when
        Set<String> response = validate(paymentDeclaration);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldHaveValidationMessageWhenPaidDateIsNull() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder()
            .withExplanation("defence")
            .withPaidDate(null)
            .build();
        //when
        Set<String> errors = validate(paymentDeclaration);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("paidDate : may not be null");
    }

    @Test
    public void shouldHaveValidationMessageWhenPaidDateIsInTheFuture() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder()
            .withExplanation("defence")
            .withPaidDate(LocalDate.now().plusYears(1))
            .build();
        //when
        Set<String> errors = validate(paymentDeclaration);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("paidDate : is in the future");
    }

    @Test
    public void shouldHaveValidationMessageWhenExplanationIsNull() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder()
            .withExplanation(null)
            .build();
        //when
        Set<String> errors = validate(paymentDeclaration);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("explanation : may not be empty");
    }

    @Test
    public void shouldHaveValidationMessageWhenExplanationIsEmpty() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder()
            .withExplanation("")
            .build();
        //when
        Set<String> errors = validate(paymentDeclaration);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("explanation : may not be empty");
    }

    @Test
    public void shouldHaveValidationMessageWhenExplanationExceedsSizeLimit() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder()
            .withExplanation(randomAlphanumeric(99001))
            .build();
        //when
        Set<String> errors = validate(paymentDeclaration);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("explanation : size must be between 0 and 99000");
    }
}
