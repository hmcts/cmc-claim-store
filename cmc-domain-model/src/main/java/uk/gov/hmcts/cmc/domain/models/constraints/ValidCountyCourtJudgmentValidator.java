package uk.gov.hmcts.cmc.domain.models.constraints;

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
    public void initialize(ValidCountyCourtJudgment constraintAnnotation) {
        // nothing to do here
    }

    @Override
    public boolean isValid(CountyCourtJudgment ccj, ConstraintValidatorContext context) {
        if (ccj == null) {
            return true;
        }

        final PaymentOption type = ccj.getPaymentOption();
        final boolean payByDateIsPopulated = ccj.getPayBySetDate().isPresent();
        final boolean repaymentPlanIsPopulated = ccj.getRepaymentPlan().isPresent();

        boolean isValidImmediately = type.equals(IMMEDIATELY) && !payByDateIsPopulated && !repaymentPlanIsPopulated;
        boolean isValidFull = type.equals(FULL_BY_SPECIFIED_DATE) && payByDateIsPopulated && !repaymentPlanIsPopulated;
        boolean isValidByInstalments = type.equals(INSTALMENTS) && !payByDateIsPopulated && repaymentPlanIsPopulated;

        return isValidImmediately || isValidFull || isValidByInstalments;
    }
}
