package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeNullError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidResidenceConstraintValidator implements ConstraintValidator<ValidResidence, Residence> {

    private static final String OTHER_DETAILS = "otherDetail";

    @Override
    public boolean isValid(Residence residence, ConstraintValidatorContext context) {
        if (residence == null) {
            return true;
        }

        Residence.ResidenceType type = residence.getType();

        if (type == Residence.ResidenceType.OTHER) {
            if (!residence.getOtherDetail().isPresent()) {
                setValidationErrors(
                    context, OTHER_DETAILS, mayNotBeNullError("type", type.getDescription())
                );
                return false;
            }
        } else {
            if (residence.getOtherDetail().isPresent()) {
                setValidationErrors(
                    context, OTHER_DETAILS, mayNotBeProvidedError("type", type.getDescription())
                );
                return false;
            }
        }

        return true;
    }
}
