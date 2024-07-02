package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidIncomeConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    @Mock ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    private final ValidIncomeConstraintValidator validator = new ValidIncomeConstraintValidator();

    @Test
    public void shouldBeValidWhenNull() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenPaymentFrequencyIsPopulated() {
        Income model = Income.builder()
            .frequency(PaymentFrequency.FOUR_WEEKS)
            .build();
        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenIncomeTypeOtherIsPopulated() {
        Income model = Income.builder()
            .type(Income.IncomeType.OTHER).otherSource("abc").build();
        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInValidWhenIncomeTypeOtherWithoutOtherSourcePopulated() {
        Income model = Income.builder()
            .type(Income.IncomeType.OTHER).build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInValidWhenDifferentIncomeTypeWithOtherNameIsPopulated() {
        Income model = Income.builder()
            .type(Income.IncomeType.JOB).otherSource("abc").build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }
}
