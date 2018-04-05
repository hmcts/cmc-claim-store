package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;

import java.math.BigDecimal;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidInterestConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
        nodeBuilderCustomizableContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    private ValidInterestConstraintValidator validator = new ValidInterestConstraintValidator();

    private static final String notProvidedMessage = "is not provided";

    private static final String rate = "rate";

    @Test
    public void shouldBeValidForNullInterest() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidForInterestWithNullType() {
        Interest interest = SampleInterest.builder()
            .withType(null)
            .build();

        assertThat(validator.isValid(interest, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInValidWithNullRateOfInterestOnDifferentRate() {
        Interest interest = SampleInterest.differentInterest(null, "some reason");

        commonExpectation(rate, notProvidedMessage);

        assertThat(validator.isValid(interest, validatorContext)).isFalse();

        commonVerify(rate, notProvidedMessage);
    }

    @Test
    public void shouldBeInValidWithZeroRateOfInterestOnDifferentRate() {
        String message = "has to be greater than zero value";
        Interest interest = SampleInterest.differentInterest(BigDecimal.ZERO, "some reason");

        commonExpectation(rate, message);

        assertThat(validator.isValid(interest, validatorContext)).isFalse();

        commonVerify(rate, message);
    }

    @Test
    public void shouldBeInValidWithDifferentRateAndNoReason() {
        String fieldName = "reason";
        Interest interest = SampleInterest.differentInterest(BigDecimal.ONE, null);

        commonExpectation(fieldName, notProvidedMessage);

        assertThat(validator.isValid(interest, validatorContext)).isFalse();

        commonVerify(fieldName, notProvidedMessage);
    }

    @Test
    public void shouldBeValidWithDifferentRateWithRateAndReason() {
        Interest interest = SampleInterest.differentInterest(BigDecimal.ONE, "Some reason");
        assertThat(validator.isValid(interest, validatorContext)).isTrue();
    }

    @Test
    public void shouldReturnInValidWithNoInterestDateAndStandardInterestRate() {
        String fieldName = "interestDate";
        Interest interest = SampleInterest.standardWithNoInterestDate();

        commonExpectation(fieldName, notProvidedMessage);

        assertThat(validator.isValid(interest, validatorContext)).isFalse();

        commonVerify(fieldName, notProvidedMessage);

    }

    @Test
    public void shouldBeInvalidWhenBreakdownInterestAndContinueToClaimAmountAndInterest() {
        String templateMessage = "either rate or specific amount should be claimed";
        Interest interest = SampleInterest.breakdownInterestBuilder()
            .withRate(new BigDecimal(8))
            .withSpecificDailyAmount(new BigDecimal(1000)).build();

        commonExpectation(rate, templateMessage);

        assertThat(validator.isValid(interest, validatorContext)).isFalse();

        commonVerify(rate, templateMessage);
    }

    @Test
    public void shouldBeInvalidWhenBreakdownInterestAndContinueToClaimWithInterestOnly() {
        Interest interest = SampleInterest.breakdownInterestBuilder()
            .withRate(new BigDecimal(8)).build();

        assertThat(validator.isValid(interest, validatorContext)).isTrue();
    }

    private void commonVerify(String fieldName, String message) {
        verify(validatorContext).disableDefaultConstraintViolation();
        verify(validatorContext).buildConstraintViolationWithTemplate(eq(message));
        verify(constraintViolationBuilder).addPropertyNode(eq(fieldName));
        verify(nodeBuilderCustomizableContext).addConstraintViolation();
    }

    private void commonExpectation(String fieldName, String message) {
        when(validatorContext
            .buildConstraintViolationWithTemplate(eq(message))).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(eq(fieldName)))
            .thenReturn(nodeBuilderCustomizableContext);
        when(nodeBuilderCustomizableContext.addConstraintViolation()).thenReturn(validatorContext);
    }

}
