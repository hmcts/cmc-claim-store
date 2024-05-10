package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class UserRoleConstraintValidator implements ConstraintValidator<UserRoleValidator, String> {

    private static final List<String> userRoles = Arrays.asList("cmc-new-features-consent-given",
        "cmc-new-features-consent-not-given");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return userRoles.contains(value) ? Boolean.TRUE : Boolean.FALSE;
    }
}
