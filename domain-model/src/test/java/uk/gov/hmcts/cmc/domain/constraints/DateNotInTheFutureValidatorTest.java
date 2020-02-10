package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DateNotInTheFutureValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private final DateNotInTheFutureConstraintValidator validator = new DateNotInTheFutureConstraintValidator();

    @Test
    public void shouldReturnTrueForValidDateInThePast() {
        assertThat(validator.isValid(LocalDate.now().minusDays(2), context)).isTrue();
    }

    @Test
    public void shouldReturnTrueForValidDateToday() {
        assertThat(validator.isValid(LocalDate.now(), context)).isTrue();
    }

    @Test
    public void shouldReturnFalseForValidDateInTheFuture() {
        assertThat(validator.isValid(LocalDate.now().plusDays(2), context)).isFalse();
    }
}
