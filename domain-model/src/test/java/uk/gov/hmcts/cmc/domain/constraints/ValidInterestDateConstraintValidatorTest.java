package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ValidInterestDateConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private final ValidInterestDateConstraintValidator validator = new ValidInterestDateConstraintValidator();

    @Test
    public void shouldBeValidForNullInterestDate() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

}
