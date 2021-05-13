package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeNullError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidExpenseConstraintValidator implements ConstraintValidator<ValidExpense, Expense> {

    private static final String OTHER_EXPENSE_NAME = "otherName";

    @Override
    public boolean isValid(Expense expense, ConstraintValidatorContext context) {
        if (expense == null) {
            return true;
        }

        Expense.ExpenseType type = expense.getType();

        if (type == Expense.ExpenseType.OTHER) {
            if (!expense.getOtherName().isPresent()) {
                setValidationErrors(
                    context, OTHER_EXPENSE_NAME, mayNotBeNullError("type", type.getDescription())
                );
                return false;
            }
        } else {
            if (expense.getOtherName().isPresent()) {
                setValidationErrors(
                    context, OTHER_EXPENSE_NAME, mayNotBeProvidedError("type", type.getDescription())
                );
                return false;
            }
        }

        return true;
    }
}
