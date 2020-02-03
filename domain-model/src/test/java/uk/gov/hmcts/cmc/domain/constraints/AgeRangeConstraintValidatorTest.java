package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AgeRangeConstraintValidatorTest {

    @Mock
    private AgeRangeValidator annotation;

    @Mock
    private ConstraintValidatorContext context;

    private final AgeRangeConstraintValidator validator = new AgeRangeConstraintValidator();

    @Before
    public void beforeEachTest() {
        validator.initialize(with(18, 150));
    }

    @Test
    public void shouldReturnTrueForNullValidationInput() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenLessThanMinimumAllowedDefault() {
        LocalDate today = LocalDate.now();
        assertThat(validator.isValid(today, context)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenGreaterThanMaximumAllowedDefault() {
        LocalDate over150Years = LocalDate.now().minusYears(151);
        assertThat(validator.isValid(over150Years, context)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenLessThanMinimumAllowed() {
        LocalDate today = LocalDate.now();
        assertThat(validator.isValid(today, context)).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenWithValid() {
        LocalDate over18Years = LocalDate.now().minusYears(19);
        assertThat(validator.isValid(over18Years, context)).isTrue();
    }

    private AgeRangeValidator with(int minYears, int maxYears) {
        when(annotation.minYears()).thenReturn(minYears);
        when(annotation.maxYears()).thenReturn(maxYears);
        return annotation;
    }
}
