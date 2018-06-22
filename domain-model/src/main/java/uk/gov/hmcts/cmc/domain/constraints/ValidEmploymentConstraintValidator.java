package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidEmploymentConstraintValidator implements ConstraintValidator<ValidEmployment, Employment> {

    @Override
    public boolean isValid(Employment employment, ConstraintValidatorContext context) {
        boolean valid = true;

        if (employment.getUnemployment().isPresent()) {
            if (employment.getEmployers().size() > 0) {
                setValidationErrors(
                    context, "employers", mayNotBeProvidedError("employment", "unemployment")
                );
                valid = false;
            }

            if (employment.getSelfEmployment().isPresent()) {
                setValidationErrors(
                    context, "selfEmployment", mayNotBeProvidedError("employment", "unemployment")
                );
                valid = false;
            }
        }

        if ((employment.getSelfEmployment().isPresent()
            || employment.getEmployers().size() > 0)
            && employment.getUnemployment().isPresent()) {
            setValidationErrors(
                context,
                "unemployment",
                mayNotBeProvidedError("employment", "selfEmployment or employers")
            );
            valid = false;

        }

        return valid;
    }
}
