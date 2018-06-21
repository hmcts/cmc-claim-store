package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidEmploymentConstraintValidator implements ConstraintValidator<ValidEmployment, Employment> {

    @Override
    public boolean isValid(Employment employment, ConstraintValidatorContext context) {
        boolean valid = true;

        if (employment.getUnemployment().isPresent()) {
            if (employment.getEmployers().size() > 0) {
                setValidationErrors(context, "employers", mayNotBeProvidedErrorForType("unemployment"));
                valid = false;
            }

            if (employment.getSelfEmployment().isPresent()) {
                setValidationErrors(context, "selfEmployment", mayNotBeProvidedErrorForType("unemployment"));
                valid = false;
            }
        }

        if (employment.getSelfEmployment().isPresent() || employment.getEmployers().size() > 0) {
            if (employment.getUnemployment().isPresent()) {
                setValidationErrors(
                    context, "selfEmployment", mayNotBeProvidedErrorForType("selfEmployment or employers")
                );
                valid = false;
            }
        }

        return valid;
    }

    private void setValidationErrors(ConstraintValidatorContext validatorContext, String fieldName, String... errors) {
        validatorContext.disableDefaultConstraintViolation();
        for (String error : errors) {
            validatorContext.buildConstraintViolationWithTemplate(error)
                .addPropertyNode(fieldName)
                .addConstraintViolation();
        }
    }

    private String mayNotBeProvidedErrorForType(String type) {
        return String.format("may not be provided when employment is '%s'", type);
    }
}
