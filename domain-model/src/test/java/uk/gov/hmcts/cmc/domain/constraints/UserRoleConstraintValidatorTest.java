package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
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

    @Test
    public void shouldReturnTrueForConsentNotGiven() {
        assertThat(validator.isValid("cmc-new-features-consent-not-given", validatorContext)).isTrue();
    }
}
