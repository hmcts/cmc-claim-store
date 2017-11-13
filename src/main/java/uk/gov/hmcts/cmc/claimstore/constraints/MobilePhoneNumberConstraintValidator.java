package uk.gov.hmcts.cmc.claimstore.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MobilePhoneNumberConstraintValidator implements ConstraintValidator<MobilePhoneNumber, String> {

    @Override
    public void initialize(final MobilePhoneNumber validator) {
        // NO-OP
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext cxt) {
        if (value == null) {
            return true;
        }

        String phone = value.replaceAll("\\(|\\)| |-|\\+", "")
            .replaceFirst("^(00)?44", "")
            .replaceFirst("^0*", "");

        return phone.startsWith("7") && phone.length() == 10;
    }
}
