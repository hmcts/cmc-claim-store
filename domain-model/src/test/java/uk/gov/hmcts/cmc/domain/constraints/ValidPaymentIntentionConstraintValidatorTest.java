package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.time.LocalDate;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidPaymentIntentionConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    private final ValidPaymentIntentionConstraintValidator validator = new ValidPaymentIntentionConstraintValidator();

    @Test
    public void shouldBeValidWhenInputIsNull() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenTypeIsImmediatelyAndPaymentDateIsValid() {
        PaymentIntention instance = PaymentIntention.builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .paymentDate(LocalDate.now())
            .build();

        assertThat(validator.isValid(instance, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsImmediatelyAndPaymentDateIsNotPopulated() {
        PaymentIntention instance = PaymentIntention.builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(instance, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsImmediatelyAndRepaymentPlanIsPopulated() {
        PaymentIntention instance = PaymentIntention.builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .paymentDate(LocalDate.now())
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);
        assertThat(validator.isValid(instance, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenTypeIsBySetDateAndPaymentDateIsValid() {
        PaymentIntention instance = PaymentIntention.builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .paymentDate(LocalDate.now().plusDays(3))
            .build();

        assertThat(validator.isValid(instance, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsBySetDateAndPaymentDateIsNotPopulated() {
        PaymentIntention instance = PaymentIntention.builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);
        assertThat(validator.isValid(instance, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsBySetDateAndRepaymentPlanIsPopulated() {
        PaymentIntention instance = PaymentIntention.builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .paymentDate(LocalDate.now().plusDays(3))
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);
        assertThat(validator.isValid(instance, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenTypeIsInstalmentsAndRepaymentPlanIsValid() {
        PaymentIntention instance = PaymentIntention.builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        assertThat(validator.isValid(instance, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsInstalmentsAndRepaymentPlanIsNotPopulated() {
        PaymentIntention instance = PaymentIntention.builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(instance, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInvalidWhenTypeIsInstalmentsAndPaymentDateIsPopulated() {
        PaymentIntention instance = PaymentIntention.builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .paymentDate(LocalDate.now().plusDays(1))
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(instance, validatorContext)).isFalse();
    }
}
