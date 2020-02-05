package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ValidInterestDateConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private final ValidInterestDateConstraintValidator validator = new ValidInterestDateConstraintValidator();

    @Test
    public void shouldBeValidForNullInterestDate() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }

}
