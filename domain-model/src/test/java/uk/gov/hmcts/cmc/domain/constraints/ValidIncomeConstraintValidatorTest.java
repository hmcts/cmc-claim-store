package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidIncomeConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private final ValidIncomeConstraintValidator validator = new ValidIncomeConstraintValidator();

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
        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }

    @Test
    public void shouldBeInValidWhenDifferentIncomeTypeWithOtherNameIsPopulated() {
        Income model = Income.builder()
            .type(Income.IncomeType.JOB).otherSource("abc").build();
        assertThat(validator.isValid(model, validatorContext)).isFalse();
    }
}
