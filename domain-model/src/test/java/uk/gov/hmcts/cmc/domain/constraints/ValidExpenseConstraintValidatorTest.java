package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidExpenseConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private final ValidExpenseConstraintValidator validator = new ValidExpenseConstraintValidator();

    @Before
    public void setUp() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(
            ConstraintValidatorContext.ConstraintViolationBuilder.class
        );

        when(builder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(validatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    }

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
        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInValidWhenOtherExpenseTypeWithOtherNameIsNotPopulated() {
        Expense model = Expense.builder()
            .type(Expense.ExpenseType.COUNCIL_TAX).otherName("abc").build();
        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeValidWhenNull() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

}
