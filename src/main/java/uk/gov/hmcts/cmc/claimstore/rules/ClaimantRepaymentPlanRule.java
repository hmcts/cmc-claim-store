package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantInvalidRepaymentPlanException;
import uk.gov.hmcts.cmc.claimstore.utils.CalculateMonthIncrement;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;

@Service
public class ClaimantRepaymentPlanRule {

    public static final String INVALID_RESPONSE_TYPE =
        "Defendant response must be part or full admission";
    public static final String INVALID_DEFENDANT_REPAYMENT_TYPE =
        "Expected defendant response to be installments or set date";
    public static final String EXPECTED_PAYMENT_INTENTION =
        "Expected payment intention to be present on defendant response";
    public static final String EXPECTED_REPAYMENT_PLAN_DEFENDANT =
        "Expected repayment plan to be present on defendant response";
    public static final String EXPECTED_REPAYMENT_PLAN_CLAIMANT =
        "Expected repayment plan to be present on claimant response";
    public static final String EXPECTED_DEFENDANT_RESPONSE =
        "Expected defendant response to be present";
    public static final String INSTALLMENT_DATE_MUST_BE_AFTER =
        "Claimant first installment date must be after %s";
    public static final String INSTALLMENT_DATE_MUST_BE_ONE_MONTH =
        "Claimant first installment date must be at least one month from now";


    public void assertClaimantRepaymentPlanIsValid(Claim claim, RepaymentPlan proposedPlan) {
        Response response = claim.getResponse().orElseThrow(() -> new ClaimantInvalidRepaymentPlanException(
            EXPECTED_DEFENDANT_RESPONSE));

        switch (getPaymentOptionForResponse(response)) {
            case INSTALMENTS:
                assertInstallmentsIsValidAgainstInstallments(proposedPlan, getRepaymentPlanForResponse(response));
                break;
            case BY_SPECIFIED_DATE:
                assertInstallmentsIsValidAgainstSetDate(proposedPlan);
                break;
            default:
                throw new ClaimantInvalidRepaymentPlanException(
                    INVALID_DEFENDANT_REPAYMENT_TYPE);
        }
    }


    private void assertInstallmentsIsValidAgainstInstallments(RepaymentPlan claimantPlan, RepaymentPlan defendantPlan) {
        if (claimantPlan == null) {
            throw new ClaimantInvalidRepaymentPlanException(EXPECTED_REPAYMENT_PLAN_CLAIMANT);
        }

        if (defendantPlan == null) {
            throw new ClaimantInvalidRepaymentPlanException(EXPECTED_REPAYMENT_PLAN_DEFENDANT);
        }

        LocalDate defendantStartDate = defendantPlan.getFirstPaymentDate();
        LocalDate monthFromNow = CalculateMonthIncrement.calculateMonthlyIncrement(LocalDate.now());
        LocalDate thresholdDate = defendantStartDate.isBefore(monthFromNow) ? defendantStartDate : monthFromNow;

        if (claimantPlan.getFirstPaymentDate().isBefore(thresholdDate)) {
            throw new ClaimantInvalidRepaymentPlanException(
                String.format(INSTALLMENT_DATE_MUST_BE_AFTER, thresholdDate));
        }
    }

    private void assertInstallmentsIsValidAgainstSetDate(RepaymentPlan claimantPlan) {
        if (claimantPlan == null) {
            throw new ClaimantInvalidRepaymentPlanException(EXPECTED_REPAYMENT_PLAN_CLAIMANT);
        }

        LocalDate oneMonthFromNow = CalculateMonthIncrement.calculateMonthlyIncrement(LocalDate.now());
        if (claimantPlan.getFirstPaymentDate().isBefore(oneMonthFromNow)) {
            throw new ClaimantInvalidRepaymentPlanException(INSTALLMENT_DATE_MUST_BE_ONE_MONTH);
        }
    }

    private PaymentOption getPaymentOptionForResponse(Response response) {
        switch (response.getResponseType()) {
            case PART_ADMISSION:
                return ((PartAdmissionResponse) response).getPaymentIntention()
                    .orElseThrow(() -> new ClaimantInvalidRepaymentPlanException(
                        EXPECTED_PAYMENT_INTENTION))
                    .getPaymentOption();
            case FULL_ADMISSION:
                return ((FullAdmissionResponse) response).getPaymentIntention().getPaymentOption();
            default:
                throw new ClaimantInvalidRepaymentPlanException(
                    INVALID_RESPONSE_TYPE);
        }
    }

    private RepaymentPlan getRepaymentPlanForResponse(Response response) {
        switch (response.getResponseType()) {
            case PART_ADMISSION:
                return ((PartAdmissionResponse) response)
                    .getPaymentIntention()
                    .orElseThrow(() -> new ClaimantInvalidRepaymentPlanException(
                        EXPECTED_PAYMENT_INTENTION))
                    .getRepaymentPlan()
                    .orElseThrow(() -> new ClaimantInvalidRepaymentPlanException(
                        EXPECTED_REPAYMENT_PLAN_DEFENDANT));
            case FULL_ADMISSION:
                return ((FullAdmissionResponse) response)
                    .getPaymentIntention()
                    .getRepaymentPlan()
                    .orElseThrow(() -> new ClaimantInvalidRepaymentPlanException(
                        EXPECTED_REPAYMENT_PLAN_DEFENDANT));
            default:
                throw new ClaimantInvalidRepaymentPlanException(
                    INVALID_RESPONSE_TYPE);

        }
    }
}


