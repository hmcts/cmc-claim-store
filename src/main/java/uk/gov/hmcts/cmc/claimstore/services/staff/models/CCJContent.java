package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RepaymentPlanContentProvider;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class CCJContent {

    private Claim claim;
    private String requestedAt;
    private String requestedDate;
    private AmountContent amount;
    private String repaymentOption;
    private String defendantDateOfBirth;

    public CCJContent(Claim claim, InterestCalculationService interestCalculationService) {
        requireNonNull(claim);
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();

        this.claim = claim;
        this.repaymentOption = RepaymentPlanContentProvider.create(countyCourtJudgment);
        this.amount = new AmountContentProvider(interestCalculationService).create(claim);
        this.requestedAt = Formatting.formatDateTime(claim.getCountyCourtJudgmentRequestedAt());
        this.requestedDate = formatDate(claim.getCountyCourtJudgmentRequestedAt());
        if (claim.getCountyCourtJudgment().getDefendant() instanceof IndividualDetails) {
            IndividualDetails details = (IndividualDetails) claim.getCountyCourtJudgment().getDefendant();
            details.getDateOfBirth()
                .ifPresent((dateOfBirth -> this.defendantDateOfBirth = formatDate(dateOfBirth)));
        }
    }

    public Claim getClaim() {
        return claim;
    }

    public AmountContent getAmount() {
        return amount;
    }

    public String getRepaymentOption() {
        return repaymentOption;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getDefendantDateOfBirth() {
        return defendantDateOfBirth;
    }

}
