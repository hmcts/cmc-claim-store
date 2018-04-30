package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption.FULL_BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption.INSTALMENTS;

public class ValidCountyCourtJudgmentValidator
    implements ConstraintValidator<ValidCountyCourtJudgment, CountyCourtJudgment> {
    
    @Override
    public boolean isValid(CountyCourtJudgment ccj, ConstraintValidatorContext context) {
        if (!isEligibleForThisValidator(ccj)) {
            return true;
        }

        PaymentOption type = ccj.getPaymentOption();
        boolean payByDateIsPopulated = ccj.getPayBySetDate().isPresent();
        boolean repaymentPlanIsPopulated = ccj.getRepaymentPlan().isPresent();

        boolean isValidImmediately = type.equals(IMMEDIATELY) && !payByDateIsPopulated && !repaymentPlanIsPopulated;
        boolean isValidFull = type.equals(FULL_BY_SPECIFIED_DATE) && payByDateIsPopulated && !repaymentPlanIsPopulated;
        boolean isValidByInstalments = type.equals(INSTALMENTS) && !payByDateIsPopulated && repaymentPlanIsPopulated;

        return isValidImmediately || isValidFull || isValidByInstalments;
    }

    private boolean isEligibleForThisValidator(CountyCourtJudgment ccj) {
        return ccj != null && ccj.getPaymentOption() != null;
    }

}
