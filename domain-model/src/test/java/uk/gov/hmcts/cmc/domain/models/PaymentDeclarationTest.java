package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePaymentDeclaration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class PaymentDeclarationTest {

    @Test
    public void shouldHaveNoValidationMessageWhenInstanceIsValid() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder().build();
        //when
        Set<String> response = validate(paymentDeclaration);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldHaveNoValidationMessageWhenInstanceIsValidWithoutPaidAmount() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder()
            .paidAmount(null)
            .build();
        //when
        Set<String> response = validate(paymentDeclaration);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldHaveValidationMessageWhenPaidDateIsNull() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder()
            .explanation("defence")
            .paidDate(null)
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
            .explanation("defence")
            .paidDate(LocalDate.now().plusYears(1))
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
            .explanation(null)
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
            .explanation("")
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
            .explanation(randomAlphanumeric(99001))
            .build();
        //when
        Set<String> errors = validate(paymentDeclaration);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("explanation : size must be between 0 and 99000");
    }

    @Test
    public void shouldHaveValidationMessageWhenPaidAmountIsNonPositive() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder()
            .paidAmount(BigDecimal.ZERO)
            .build();
        //when
        Set<String> errors = validate(paymentDeclaration);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("paidAmount : must be greater than or equal to 0.01");
    }
}
