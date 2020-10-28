package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidEmploymentConstraintValidator implements ConstraintValidator<ValidEmployment, Employment> {

    public static class Fields {
        public static final String EMPLOYERS = "employers";
        public static final String SELF_EMPLOYMENT = "selfEmployment";
        public static final String UNEMPLOYMENT = "unemployment";
        public static final String EMPLOYMENT = "employment";
    }

    @Override
    public boolean isValid(Employment employment, ConstraintValidatorContext context) {
        boolean valid = true;

        if (employment == null) {
            return true;
        }

        if (employment.getUnemployment().isPresent()) {
            if (employment.getEmployers().size() > 0) {
                setValidationErrors(
                    context, Fields.EMPLOYERS, mayNotBeProvidedError(Fields.EMPLOYMENT, Fields.UNEMPLOYMENT)
                );
                valid = false;
            }

            if (employment.getSelfEmployment().isPresent()) {
                setValidationErrors(
                    context, Fields.SELF_EMPLOYMENT, mayNotBeProvidedError(Fields.EMPLOYMENT, Fields.UNEMPLOYMENT)
                );
                valid = false;
            }
        }

        if ((employment.getSelfEmployment().isPresent()
            || employment.getEmployers().size() > 0)
            && employment.getUnemployment().isPresent()) {
            setValidationErrors(
                context,
                Fields.UNEMPLOYMENT,
                mayNotBeProvidedError(Fields.EMPLOYMENT, Fields.SELF_EMPLOYMENT + " or " + Fields.EMPLOYERS)
            );
            valid = false;

        }

        return valid;
    }
}
