package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countyCourtJudgment.AmountRemainingContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countyCourtJudgment.RepaymentPlanContentProvider;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class CCJContent {

    private String requestedAt;
    private String claimReferenceNumber;
    private String claimantName;
    private String requestedDate;
    private String defendantName;
    private Address defendantAddress;
    private String defendantEmail;
    private String amountToPayByDefendant;
    private String repaymentOption;
    private String signerName;
    private String signerRole;
    private String defendantDateOfBirth;

    public CCJContent(Claim claim, InterestCalculationService interestCalculationService) {
        requireNonNull(claim);
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();

        this.claimReferenceNumber = claim.getReferenceNumber();
        this.defendantName = claim.getClaimData().getDefendant().getName();
        this.defendantAddress = claim.getClaimData().getDefendant().getAddress();
        this.repaymentOption = RepaymentPlanContentProvider.create(countyCourtJudgment);
        this.defendantEmail = claim.getClaimData().getDefendant().getEmail().orElse(null);
        this.amountToPayByDefendant = AmountRemainingContentProvider.calculate(interestCalculationService, claim);
        this.requestedAt = Formatting.formatDateTime(claim.getCountyCourtJudgmentRequestedAt());
        this.claimantName = claim.getClaimData().getClaimant().getName();
        this.requestedDate = formatDate(claim.getCountyCourtJudgmentRequestedAt());
        if (claim.getCountyCourtJudgment().getDefendant() instanceof IndividualDetails) {
            IndividualDetails details = (IndividualDetails) claim.getCountyCourtJudgment().getDefendant();
            details.getDateOfBirth()
                .ifPresent((dateOfBirth -> this.defendantDateOfBirth = formatDate(dateOfBirth)));
        }

        countyCourtJudgment.getStatementOfTruth().ifPresent(statementOfTruth -> {
            this.signerName = statementOfTruth.getSignerName();
            this.signerRole = statementOfTruth.getSignerRole();
        });
    }


    public String getClaimReferenceNumber() {
        return claimReferenceNumber;
    }

    public String getDefendantName() {
        return defendantName;
    }

    public Address getDefendantAddress() {
        return defendantAddress;
    }

    public String getDefendantEmail() {
        return defendantEmail;
    }

    public String getRepaymentOption() {
        return repaymentOption;
    }

    public String getAmountToPayByDefendant() {
        return amountToPayByDefendant;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public String getSignerName() {
        return signerName;
    }

    public String getSignerRole() {
        return signerRole;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getDefendantDateOfBirth() {
        return defendantDateOfBirth;
    }

}
