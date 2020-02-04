package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ValidIncomeConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    private final ValidIncomeConstraintValidator validator = new ValidIncomeConstraintValidator();

    @Test
    public void shouldBeValidWhenNull() {
        assertThat(validator.isValid(null, validatorContext)).isTrue();
    }
}
