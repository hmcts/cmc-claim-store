package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeNullError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidIncomeConstraintValidator implements ConstraintValidator<ValidIncome, Income> {

    private static final String OTHER_DETAILS = "otherSource";

    @Override
    public boolean isValid(Income income, ConstraintValidatorContext context) {
        if (income == null) {
            return true;
        }

        Income.IncomeType type = income.getType();

        if (type == Income.IncomeType.OTHER) {
            if (!income.getOtherSource().isPresent()) {
                setValidationErrors(
                    context, OTHER_DETAILS, mayNotBeNullError("type", type.getDescription())
                );
                return false;
            }
        } else {
            if (income.getOtherSource().isPresent()) {
                setValidationErrors(
                    context, OTHER_DETAILS, mayNotBeProvidedError("type", type.getDescription())
                );
                return false;
            }
        }

        return true;
    }
}
