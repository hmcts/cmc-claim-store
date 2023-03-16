
package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.constraints.utils.ScotlandOrNorthernIrelandPostcodeUtil;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PostcodeConstraintValidator implements ConstraintValidator<Postcode, String> {

    private static final String REGEX = "^([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y]"
        + "[0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9]?[A-Za-z]))))[0-9][A-Za-z]{2})$";

    private static final Pattern pattern = Pattern.compile(REGEX);

    private final ScotlandOrNorthernIrelandPostcodeUtil scotlandOrNorthernIrelandPostcodeUtil = new
        ScotlandOrNorthernIrelandPostcodeUtil();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        // check if Scottish or Irish
        boolean isScottishOrIrish = scotlandOrNorthernIrelandPostcodeUtil.postcodeInScotlandOrNorthernIreland(value);
        if (isScottishOrIrish) {
            return false;
        }

        String normalised = value.replaceAll("\\s", "");
        return pattern.matcher(normalised).matches();
    }
}
