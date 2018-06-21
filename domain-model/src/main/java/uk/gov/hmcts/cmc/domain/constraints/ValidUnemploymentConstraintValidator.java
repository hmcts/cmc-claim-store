package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidUnemploymentConstraintValidator implements ConstraintValidator<ValidEmployment, Unemployment> {

    static class Fields {
        static final String UNEMPLOYED = "unemployed";
        static final String IS_RETIRED = "isRetired";
        static final String OTHER = "other";
    }

    @Override
    public boolean isValid(Unemployment unemployment, ConstraintValidatorContext context) {
        boolean valid = true;
        boolean isRetiredPopulated = unemployment.isRetired();
        boolean otherPopulated = unemployment.getOther().isPresent();
        boolean unemployedPopulated = unemployment.getUnemployed().isPresent();

        if (isRetiredPopulated) {
            if (otherPopulated) {
                setValidationErrors(context, Fields.OTHER, mayNotBeProvidedError(Fields.OTHER, Fields.IS_RETIRED));
                valid = false;
            }

            if (unemployedPopulated) {
                setValidationErrors(
                    context, Fields.UNEMPLOYED, mayNotBeProvidedError(Fields.UNEMPLOYED, Fields.IS_RETIRED)
                );
                valid = false;
            }
        }

        if (otherPopulated) {
            if (isRetiredPopulated) {
                setValidationErrors(context, Fields.IS_RETIRED, mayNotBeProvidedError(Fields.IS_RETIRED, Fields.OTHER));
                valid = false;
            }

            if (unemployedPopulated) {
                setValidationErrors(context, Fields.UNEMPLOYED, mayNotBeProvidedError(Fields.UNEMPLOYED, Fields.OTHER));
                valid = false;
            }
        }

        if (unemployedPopulated) {
            if (isRetiredPopulated) {
                setValidationErrors(
                    context, Fields.IS_RETIRED, mayNotBeProvidedError(Fields.IS_RETIRED, Fields.UNEMPLOYED)
                );
                valid = false;
            }

            if (otherPopulated) {
                setValidationErrors(context, Fields.OTHER, mayNotBeProvidedError(Fields.OTHER, Fields.UNEMPLOYED));
                valid = false;
            }
        }

        return valid;
    }
}
