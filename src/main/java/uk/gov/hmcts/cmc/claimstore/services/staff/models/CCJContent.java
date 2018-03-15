package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RepaymentPlanContentProvider;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class CCJContent {

    private Claim claim;
    private String requestedAt;
    private String requestedDate;
    private AmountContent amount;
    private RepaymentPlan repaymentOption;
    private String defendantDateOfBirth;
    private PaymentOption paymentOption;
    private String firstInstalmentPaymentDate;
    private String paymentFrequency;

    public CCJContent(Claim claim, InterestCalculationService interestCalculationService) {
        requireNonNull(claim);

        this.claim = claim;
        this.amount = new AmountContentProvider(interestCalculationService).create(claim);
        claim.getCountyCourtJudgment().getDefendantDateOfBirth()
            .ifPresent((dateOfBirth -> this.defendantDateOfBirth = formatDate(dateOfBirth)));
        this.repaymentOption = RepaymentPlanContentProvider.create(claim.getCountyCourtJudgment()).orElse(null);
        this.requestedAt = Formatting.formatDateTime(claim.getCountyCourtJudgmentRequestedAt());
        this.requestedDate = formatDate(claim.getCountyCourtJudgmentRequestedAt());
        this.paymentOption = claim.getCountyCourtJudgment().getPaymentOption();
        this.firstInstalmentPaymentDate = formatDate(repaymentOption.getFirstPaymentDate());
        this.paymentFrequency = repaymentOption.getPaymentSchedule().getDescription();
    }

    public Claim getClaim() {
        return claim;
    }

    public AmountContent getAmount() {
        return amount;
    }

    public String getDefendantDateOfBirth() {
        return defendantDateOfBirth;
    }

    public RepaymentPlan getRepaymentOption() {
        return repaymentOption;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public String getFirstInstalmentPaymentDate() {
        return firstInstalmentPaymentDate;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }
}
