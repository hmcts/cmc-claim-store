package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RepaymentPlanContentProvider;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class CCJContent {

    private Claim claim;
    private String requestedAt;
    private String requestedDate;
    private AmountContent amount;
    private String defendantDateOfBirth;
    private RepaymentPlanContent repayment;

    public CCJContent(Claim claim, InterestCalculationService interestCalculationService, RepaymentPlanContentProvider repaymentPlanContentProvider) {
        requireNonNull(claim);

        this.claim = claim;
        this.amount = new AmountContentProvider(interestCalculationService).create(claim);
        claim.getCountyCourtJudgment().getDefendantDateOfBirth()
            .ifPresent((dateOfBirth -> this.defendantDateOfBirth = formatDate(dateOfBirth)));
        this.requestedAt = Formatting.formatDateTime(claim.getCountyCourtJudgmentRequestedAt());
        this.requestedDate = formatDate(claim.getCountyCourtJudgmentRequestedAt());
        this.repayment = repaymentPlanContentProvider.create(claim.getCountyCourtJudgment());
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

    public String getRequestedDate() {
        return requestedDate;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public RepaymentPlanContent getRepayment() {
        return repayment;
    }
}
