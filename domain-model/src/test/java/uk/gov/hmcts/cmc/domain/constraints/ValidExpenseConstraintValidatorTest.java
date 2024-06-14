package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidExpenseConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    private final ValidExpenseConstraintValidator validator = new ValidExpenseConstraintValidator();

    @Test
    public void shouldBeValidWhenPayemtnFrequencyIsPopulated() {
        Expense model = Expense.builder()
            .frequency(PaymentFrequency.FOUR_WEEKS)
            .build();
        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeValidWhenExpenseTypeOtherIsPopulated() {
        Expense model = Expense.builder()
            .type(Expense.ExpenseType.OTHER).otherName("abc").build();
        assertThat(validator.isValid(model, validatorContext)).isTrue();
    }

    @Test
    public void shouldBeInValidWhenExpenseTypeOtherIsNotPopulated() {
        Expense model = Expense.builder()
            .type(Expense.ExpenseType.OTHER).build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInValidWhenOtherExpenseTypeWithOtherNameIsNotPopulated() {
        Expense model = Expense.builder()
            .type(Expense.ExpenseType.COUNCIL_TAX).otherName("abc").build();

        when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenNull() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

}
