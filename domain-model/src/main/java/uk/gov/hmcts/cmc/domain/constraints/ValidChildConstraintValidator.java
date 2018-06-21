package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.BETWEEN_11_AND_15;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.BETWEEN_16_AND_19;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.UNDER_11;

public class ValidChildConstraintValidator implements ConstraintValidator<ValidChild, Child> {

    static class Fields {
        static final String NO_OF_CHILDREN_LIVING_WITH_YOU = "numberOfChildrenLivingWithYou";
        static final String NUMBER_OF_CHILDREN = "numberOfChildren";
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

        Optional<Integer> numberOfChildrenLivingWithYou = child.getNumberOfChildrenLivingWithYou();
        int valueNoOfChildrenLivingWithYou = numberOfChildrenLivingWithYou.orElse(0);

        if (!numberOfChildrenLivingWithYou.isPresent()) {
            setValidationErrors(context, Fields.NO_OF_CHILDREN_LIVING_WITH_YOU, mayNotBeNullErrorForType(type));
            valid = false;
        }

        if (valueNoOfChildrenLivingWithYou > child.getNumberOfChildren()) {
            setValidationErrors(
                context,
                Fields.NUMBER_OF_CHILDREN,
                "numberOfChildren must not be less than numberOfChildrenLivingWithYou"
            );
            valid = false;
        }

        return valid;
    }

    private boolean validateYoungChild(Child child, ConstraintValidatorContext context) {
        boolean valid = true;
        String type = UNDER_11 + " or " + BETWEEN_11_AND_15;

        if (child.getNumberOfChildrenLivingWithYou().isPresent()) {
            setValidationErrors(context, Fields.NO_OF_CHILDREN_LIVING_WITH_YOU, mayNotBeProvidedErrorForType(type));
            valid = false;
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
        return String.format("may not be provided when child is '%s'", type);
    }

    private String mayNotBeNullErrorForType(String type) {
        return String.format("may not be null when child is '%s'", type);
    }
}
