package uk.gov.hmcts.cmc.domain.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserRoleConstraintValidator implements ConstraintValidator<UserRoleValidator, String> {

    private UserRoleConstraintValidator() {
        throw new IllegalStateException("Utility class");
    }

    public static class Fields {
        public static final String CONSENT_GIVEN_ROLE = "cmc-new-features-consent-given";
        public static final String CONSENT_NOT_GIVEN_ROLE = "cmc-new-features-consent-not-given";
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return (Fields.CONSENT_GIVEN_ROLE.equals(value) || Fields.CONSENT_NOT_GIVEN_ROLE.equals(value))
            ? Boolean.TRUE : Boolean.FALSE;
    }
}
