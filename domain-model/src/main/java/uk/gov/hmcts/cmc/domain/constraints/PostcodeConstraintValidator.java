
package uk.gov.hmcts.cmc.domain.constraints;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PostcodeConstraintValidator implements ConstraintValidator<Postcode, String> {

    private static final String PATTERN = "^([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y]"
        + "[0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9]?[A-Za-z]))))[0-9][A-Za-z]{2})$";

    @Override
    public void initialize(Postcode constraintAnnotation) {
        // nothing to do here
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String normalised = value.replaceAll(" ", "");
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(normalised);
        return matcher.matches();
    }
}
