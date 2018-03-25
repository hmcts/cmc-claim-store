package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RepaymentPlanContentProvider;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

import java.time.LocalDateTime;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class CCJContent {

    private Map<String, Object> claim;
    private String requestedAt;
    private String requestedDate;
    private AmountContent amount;
    private String defendantDateOfBirth;
    private RepaymentPlanContent repaymentPlan;

    public CCJContent(Map<String, Object> claim,
                      CountyCourtJudgment countyCourtJudgment,
                      LocalDateTime countyCourtJudgmentRequestedAt,
                      AmountContent amount) {
        requireNonNull(claim);
        requireNonNull(countyCourtJudgment);
        requireNonNull(countyCourtJudgmentRequestedAt);
        requireNonNull(amount);

        this.claim = claim;
        this.amount = amount;
        countyCourtJudgment.getDefendantDateOfBirth()
            .ifPresent((dateOfBirth -> this.defendantDateOfBirth = formatDate(dateOfBirth)));
        this.repaymentPlan = RepaymentPlanContentProvider.create(countyCourtJudgment);
        this.requestedAt = Formatting.formatDateTime(countyCourtJudgmentRequestedAt);
        this.requestedDate = formatDate(countyCourtJudgmentRequestedAt);
    }

    public Map<String, Object> getClaim() {
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

    public RepaymentPlanContent getRepaymentPlan() {
        return repaymentPlan;
    }
}
