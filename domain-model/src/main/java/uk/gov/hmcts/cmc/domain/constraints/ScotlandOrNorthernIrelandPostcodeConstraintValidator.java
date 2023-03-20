package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.constraints.utils.ScotlandOrNorthernIrelandPostcodeUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ScotlandOrNorthernIrelandPostcodeConstraintValidator implements
    ConstraintValidator<ScotlandOrNorthernIrelandPostcode, String> {

    private final ScotlandOrNorthernIrelandPostcodeUtil scotlandOrNorthernIrelandPostcodeUtil = new
        ScotlandOrNorthernIrelandPostcodeUtil();

    @Override
    public void initialize(ScotlandOrNorthernIrelandPostcode constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isEmpty()) {
            return false;
        }

        // check if Scottish or Irish
        boolean isScottishOrIrish = scotlandOrNorthernIrelandPostcodeUtil.postcodeInScotlandOrNorthernIreland(value);
        if (isScottishOrIrish) {
            return false;
        }

        return true;
    }
}
