package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.Interest;
import uk.gov.hmcts.cmc.claimstore.models.InterestDate;
import uk.gov.hmcts.cmc.claimstore.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.claimstore.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

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

        this.claimReferenceNumber = claim.getReferenceNumber();
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();
        this.defendantName = claim.getClaimData().getDefendant().getName();
        this.defendantAddress = claim.getClaimData().getDefendant().getAddress();
        this.repaymentOption = countyCourtJudgment.getPaymentOption().getDescription();
        this.defendantEmail = claim.getClaimData().getDefendant().getEmail().orElse(null);
        setAmountToPayByDefendant(interestCalculationService, claim);
        this.requestedAt = Formatting.formatDateTime(claim.getCountyCourtJudgmentRequestedAt());
        this.claimantName = claim.getClaimData().getClaimant().getName();
        if (claim.getCountyCourtJudgment().getDefendant() instanceof IndividualDetails) {
            IndividualDetails details = (IndividualDetails) claim.getCountyCourtJudgment().getDefendant();
            details.getDateOfBirth()
                .ifPresent((dateOfBirth -> this.defendantDateOfBirth = Formatting.formatDate(dateOfBirth)));
        }
        this.requestedDate = Formatting.formatDate(claim.getCountyCourtJudgmentRequestedAt());

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

    private void setAmountToPayByDefendant(
        final InterestCalculationService interestCalculationService,
        final Claim claim
    ) {
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();
        if (countyCourtJudgment.getPaymentOption() == PaymentOption.INSTALMENTS) {
            countyCourtJudgment.getRepaymentPlan().ifPresent(
                repaymentPlan -> this.amountToPayByDefendant = formatMoney(repaymentPlan.getRemainingAmount())
            );
        } else if (countyCourtJudgment.getPaymentOption() == PaymentOption.FULL_BY_SPECIFIED_DATE
            || countyCourtJudgment.getPaymentOption() == PaymentOption.IMMEDIATELY) {
            this.amountToPayByDefendant = calculateRemainingAmount(
                interestCalculationService,
                claim,
                claim.getCountyCourtJudgmentRequestedAt().toLocalDate()
            );
        }
    }

    private String calculateRemainingAmount(
        final InterestCalculationService interestCalculationService,
        final Claim claim,
        final LocalDate toDate
    ) {
        BigDecimal claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
        BigDecimal paidAmount = claim.getCountyCourtJudgment().getPaidAmount() != null
            ? claim.getCountyCourtJudgment().getPaidAmount() : BigDecimal.ZERO;
        BigDecimal interestAmount = getInterestAmount(
            interestCalculationService, claim, toDate, claimAmount, paidAmount
        );
        return formatMoney(claimAmount.add(interestAmount).subtract(paidAmount));
    }

    private BigDecimal getInterestAmount(
        final InterestCalculationService interestCalculationService,
        final Claim claim, LocalDate toDate,
        final BigDecimal claimAmount,
        final BigDecimal paidAmount) {
        if (!claim.getClaimData().getInterest().getType().equals(Interest.InterestType.NO_INTEREST)) {
            return interestCalculationService.calculateInterest(
                claimAmount.subtract(paidAmount),
                claim.getClaimData().getInterest().getRate(),
                getInterestFromDate(claim),
                toDate
            );
        }
        return BigDecimal.ZERO;
    }

    private LocalDate getInterestFromDate(final Claim claim) {
        if (claim.getClaimData().getInterestDate().getType().equals(InterestDate.InterestDateType.CUSTOM)) {
            InterestDate interestDate = claim.getClaimData().getInterestDate();
            return interestDate.getDate();
        } else {
            return claim.getCreatedAt().toLocalDate();
        }
    }
}
