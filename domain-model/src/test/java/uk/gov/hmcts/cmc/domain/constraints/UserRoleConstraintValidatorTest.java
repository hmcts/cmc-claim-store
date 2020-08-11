package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class UserRoleConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;
    private final UserRoleConstraintValidator validator = new UserRoleConstraintValidator();

    @Test
    public void shouldReturnTrueForValidRole() {
        assertThat(validator.isValid("cmc-new-features-consent-given", validatorContext)).isTrue();
    }

    @Test
    public void shouldReturnFalseForInvalidRole() {
        assertThat(validator.isValid("InvalidRole", validatorContext)).isFalse();
    }
}
