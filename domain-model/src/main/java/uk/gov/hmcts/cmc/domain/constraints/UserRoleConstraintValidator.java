package uk.gov.hmcts.cmc.domain.constraints;

import java.util.Arrays;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserRoleConstraintValidator implements ConstraintValidator<UserRole, String> {
    public static final List<String> userRoles =
        Arrays.asList("cmc-new-features-consent-given",
            "cmc-new-features-consent-not-given");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return userRoles.contains(value);
    }
}
