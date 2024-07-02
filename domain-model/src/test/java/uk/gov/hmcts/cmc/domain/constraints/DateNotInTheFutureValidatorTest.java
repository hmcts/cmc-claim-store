package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
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
