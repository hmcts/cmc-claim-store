package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidUnemploymentConstraintValidator implements ConstraintValidator<ValidEmployment, Unemployment> {

    private static final String UNEMPLOYED = "unemployed";
    private static final String IS_RETIRED = "isRetired";
    private static final String OTHER = "other";
    private static final String UNEMPLOYMENT = "unemployment";

    private boolean retiredPopulated(Boolean otherPopulated, boolean unemployedPopulated,
                                     ConstraintValidatorContext context) {
        boolean valid = true;
        if (Boolean.TRUE.equals(otherPopulated)) {
            setValidationErrors(context, OTHER, mayNotBeProvidedError(UNEMPLOYMENT, IS_RETIRED));
            valid = false;
        }
        if (Boolean.TRUE.equals(unemployedPopulated)) {
            setValidationErrors(
                context, UNEMPLOYED, mayNotBeProvidedError(UNEMPLOYMENT, IS_RETIRED));
            valid = false;
        }
        return valid;
    }

    private boolean otherPopulated(boolean isRetiredPopulated, boolean unemployedPopulated,
                                   ConstraintValidatorContext context) {
        boolean valid = true;
        if (Boolean.TRUE.equals(isRetiredPopulated)) {
            setValidationErrors(context, IS_RETIRED, mayNotBeProvidedError(UNEMPLOYMENT, OTHER));
            valid = false;
        }
        if (Boolean.TRUE.equals(unemployedPopulated)) {
            setValidationErrors(context, UNEMPLOYED, mayNotBeProvidedError(UNEMPLOYMENT,
                OTHER));
            valid = false;
        }
        return  valid;
    }

    private boolean unemployedPopulated(boolean isRetiredPopulated, boolean otherPopulated,
                                        ConstraintValidatorContext context) {
        boolean valid = true;
        if (Boolean.TRUE.equals(isRetiredPopulated)) {
            setValidationErrors(context, IS_RETIRED, mayNotBeProvidedError(UNEMPLOYMENT,
                UNEMPLOYED));
            valid = false;
        }
        if (Boolean.TRUE.equals(otherPopulated)) {
            setValidationErrors(context, OTHER, mayNotBeProvidedError(UNEMPLOYMENT,
                UNEMPLOYED));
            valid = false;
        }
        return valid;
    }

    @Override
    public boolean isValid(Unemployment unemployment, ConstraintValidatorContext context) {
        if (unemployment == null) {
            return true;
        }

        boolean valid = true;
        boolean isRetiredPopulated = unemployment.isRetired();
        boolean otherPopulated = unemployment.getOther().isPresent();
        boolean unemployedPopulated = unemployment.getUnemployed().isPresent();

        if (isRetiredPopulated) {
            valid = retiredPopulated(otherPopulated, unemployedPopulated, context);
        } else if (otherPopulated) {
            valid = otherPopulated(isRetiredPopulated, unemployedPopulated, context);
        } else if (unemployedPopulated) {
            valid = unemployedPopulated(isRetiredPopulated, otherPopulated, context);
        }
        return valid;
    }
}
