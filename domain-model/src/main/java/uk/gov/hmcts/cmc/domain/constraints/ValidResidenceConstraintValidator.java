package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidResidenceConstraintValidator implements ConstraintValidator<ValidResidence, Residence> {

    static class Fields {
        static final String OTHER_DETAILS = "otherDetail";
    }

    @Override
    public boolean isValid(Residence residence, ConstraintValidatorContext context) {
        Residence.ResidenceType type = residence.getType();

        if (type == Residence.ResidenceType.OTHER) {
            if (!residence.getOtherDetail().isPresent() || residence.getOtherDetail().get().isEmpty()) {
                setValidationErrors(context, Fields.OTHER_DETAILS, mayNotBeNullErrorForType(type.getDescription()));
                return false;
            }
        } else {
            if (residence.getOtherDetail().isPresent()) {
                setValidationErrors(context, Fields.OTHER_DETAILS, mayNotBeProvidedErrorForType(type.getDescription()));
                return false;
            }
        }

        return true;
    }

    private void setValidationErrors(ConstraintValidatorContext validatorContext, String fieldName, String... errors) {
        validatorContext.disableDefaultConstraintViolation();
        for (String error : errors) {
            validatorContext.buildConstraintViolationWithTemplate(error)
                .addPropertyNode(fieldName)
                .addConstraintViolation();
        }
    }

    private String mayNotBeNullErrorForType(String type) {
        return String.format("may not be null when residence type is '%s'", type);
    }

    private String mayNotBeProvidedErrorForType(String type) {
        return String.format("may not be provided when residence type is '%s'", type);
    }
}
