package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeNullError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.BETWEEN_11_AND_15;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.BETWEEN_16_AND_19;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.UNDER_11;

public class ValidChildConstraintValidator implements ConstraintValidator<ValidChild, Child> {

    public static class Fields {
        public static final String NO_OF_CHILDREN_LIVING_WITH_YOU = "numberOfChildrenLivingWithYou";
        public static final String NUMBER_OF_CHILDREN = "numberOfChildren";
    }

    @Override
    public boolean isValid(Child child, ConstraintValidatorContext context) {
        if (child == null) {
            return true;
        }

        switch (child.getAgeGroupType()) {
            case UNDER_11:
            case BETWEEN_11_AND_15:
                return validateYoungChild(child, context);
            default:
                return validateOlderChild(child, context);
        }
    }

    private boolean validateOlderChild(Child child, ConstraintValidatorContext context) {
        boolean valid = true;
        String type = BETWEEN_16_AND_19.toString();

        if (!child.getNumberOfChildrenLivingWithYou().isPresent()) {
            setValidationErrors(context, Fields.NO_OF_CHILDREN_LIVING_WITH_YOU, mayNotBeNullError("child", type));
            valid = false;
        }

        if (child.getNumberOfChildrenLivingWithYou().orElse(0) > child.getNumberOfChildren()) {
            setValidationErrors(
                context,
                Fields.NO_OF_CHILDREN_LIVING_WITH_YOU,
                "numberOfChildrenLivingWithYou must not be greater than numberOfChildren "
            );
            valid = false;
        }

        return valid;
    }

    private boolean validateYoungChild(Child child, ConstraintValidatorContext context) {
        boolean valid = true;
        String type = UNDER_11 + " or " + BETWEEN_11_AND_15;

        if (child.getNumberOfChildrenLivingWithYou().isPresent()) {
            setValidationErrors(context, Fields.NO_OF_CHILDREN_LIVING_WITH_YOU, mayNotBeProvidedError("child", type));
            valid = false;
        }

        return valid;
    }
}
