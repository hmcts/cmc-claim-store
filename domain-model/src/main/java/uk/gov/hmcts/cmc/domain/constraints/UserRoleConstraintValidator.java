package uk.gov.hmcts.cmc.domain.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserRoleConstraintValidator implements ConstraintValidator<UserRoleValidator, String> {

    private static final String CONSENT_GIVEN_ROLE = "cmc-new-features-consent-given";
    private static final String CONSENT_NOT_GIVEN_ROLE = "cmc-new-features-consent-not-given";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return (CONSENT_GIVEN_ROLE.equals(value) || CONSENT_NOT_GIVEN_ROLE.equals(value))
            ? Boolean.TRUE : Boolean.FALSE;
    }
}
