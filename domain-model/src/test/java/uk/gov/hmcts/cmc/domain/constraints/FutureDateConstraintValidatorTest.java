package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FutureDateConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private final FutureDateConstraintValidator validator = new FutureDateConstraintValidator();

    @Test
    public void shouldReturnTrueForNullArgument() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void shouldReturnFalseForToday() {
        assertThat(validator.isValid(LocalDate.now(), context)).isFalse();
    }

    @Test
    public void shouldReturnFalseForYesterday() {
        assertThat(validator.isValid(LocalDate.now().minusDays(1), context)).isFalse();
    }

    @Test
    public void shouldReturnTrueForTomorrow() {
        assertThat(validator.isValid(LocalDate.now().plusDays(1), context)).isTrue();
    }

}
