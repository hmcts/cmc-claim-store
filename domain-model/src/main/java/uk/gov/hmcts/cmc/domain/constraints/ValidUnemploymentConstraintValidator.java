package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidUnemploymentConstraintValidator implements ConstraintValidator<ValidEmployment, Unemployment> {

    public static class Fields {
        public static final String UNEMPLOYED = "unemployed";
        public static final String IS_RETIRED = "isRetired";
        public static final String OTHER = "other";
        public static final String UNEMPLOYMENT = "unemployment";
    }

    private boolean retiredPopulated(Boolean otherPopulated, boolean unemployedPopulated,
                                     ConstraintValidatorContext context) {
        boolean valid = true;
        if (otherPopulated) {
            setValidationErrors(context, Fields.OTHER, mayNotBeProvidedError(Fields.UNEMPLOYMENT,
                Fields.IS_RETIRED));
            valid = false;
        }
        if (unemployedPopulated) {
            setValidationErrors(
                context, Fields.UNEMPLOYED, mayNotBeProvidedError(Fields.UNEMPLOYMENT, Fields.IS_RETIRED));
            valid = false;
        }
        return valid;
    }

    private boolean otherPopulated(boolean isRetiredPopulated, boolean unemployedPopulated,
                                   ConstraintValidatorContext context) {
        boolean valid = true;
        if (isRetiredPopulated) {
            setValidationErrors(context, Fields.IS_RETIRED, mayNotBeProvidedError(Fields.UNEMPLOYMENT,
                Fields.OTHER));
            valid = false;
        }
        if (unemployedPopulated) {
            setValidationErrors(context, Fields.UNEMPLOYED, mayNotBeProvidedError(Fields.UNEMPLOYMENT,
                Fields.OTHER));
            valid = false;
        }
        return  valid;
    }

    private boolean unemployedPopulated(boolean isRetiredPopulated, boolean otherPopulated,
                                        ConstraintValidatorContext context) {
        boolean valid = true;
        if (isRetiredPopulated) {
            setValidationErrors(context, Fields.IS_RETIRED, mayNotBeProvidedError(Fields.UNEMPLOYMENT,
                Fields.UNEMPLOYED));
            valid = false;
        }
        if (otherPopulated) {
            setValidationErrors(context, Fields.OTHER, mayNotBeProvidedError(Fields.UNEMPLOYMENT,
                Fields.UNEMPLOYED));
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
