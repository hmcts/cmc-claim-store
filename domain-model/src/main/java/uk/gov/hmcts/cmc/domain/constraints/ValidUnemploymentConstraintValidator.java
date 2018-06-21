package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidUnemploymentConstraintValidator implements ConstraintValidator<ValidEmployment, Unemployment> {

    @Override
    public boolean isValid(Unemployment unemployment, ConstraintValidatorContext context) {
        boolean valid = true;
        boolean isRetiredPopulated = unemployment.isRetired();
        boolean otherPopulated = unemployment.getOther().isPresent() && unemployment.getOther().get().isEmpty();
        boolean unemployedPopulated = unemployment.getUnemployed().isPresent();

        if (isRetiredPopulated) {
            if (otherPopulated) {
                setValidationErrors(context, "other", mayNotBeProvidedErrorForType("isRetired"));
                valid = false;
            }

            if (unemployedPopulated) {
                setValidationErrors(context, "unemployed", mayNotBeProvidedErrorForType("isRetired"));
                valid = false;
            }
        }

        if (otherPopulated) {
            if (isRetiredPopulated) {
                setValidationErrors(context, "isRetired", mayNotBeProvidedErrorForType("other"));
                valid = false;
            }

            if (unemployedPopulated) {
                setValidationErrors(context, "unemployed", mayNotBeProvidedErrorForType("other"));
                valid = false;
            }
        }

        if (unemployedPopulated) {
            if (isRetiredPopulated) {
                setValidationErrors(context, "isRetired", mayNotBeProvidedErrorForType("unemployed"));
                valid = false;
            }

            if (otherPopulated) {
                setValidationErrors(context, "other", mayNotBeProvidedErrorForType("unemployed"));
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
