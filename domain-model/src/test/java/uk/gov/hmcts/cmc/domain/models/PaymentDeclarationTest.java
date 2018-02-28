package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePaymentDeclaration;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class PaymentDeclarationTest {

    @Test
    public void shouldBeSuccessfulValidationForWhenDidYouPay() {
        //given
        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.validDefaults();
        //when
        Set<String> response = validate(paymentDeclaration);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForNullExplanation() {
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
    public void shouldHaveValidationMessagesWhenExplanationExceedsSizeLimint() {
        //given
        String explanation = new ResourceReader().read("/defence_exceeding_size_limit.text");

        PaymentDeclaration paymentDeclaration = SamplePaymentDeclaration.builder()
            .withExplanation(explanation)
            .build();
        //when
        Set<String> errors = validate(paymentDeclaration);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("explanation : size must be between 0 and 99000");
    }

    @Test
    public void shouldHaveValidationMessagesWhenExplanationIsEmpty() {
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
    public void shouldHaveValidationMessagesWhenPaidDateIsInTheFuture() {
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
    public void shouldHaveValidationMessagesWhenPaidDateIsNull() {
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
}
