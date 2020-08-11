package uk.gov.hmcts.cmc.domain.constraints;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserRoleConstraintValidator implements ConstraintValidator<UserRole, String> {

    private List<String> roles = UserRole.userRoles;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return roles.contains(value);
    }
}
