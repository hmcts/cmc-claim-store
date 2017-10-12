package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.Interest;
import uk.gov.hmcts.cmc.claimstore.models.InterestDate;
import uk.gov.hmcts.cmc.claimstore.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.claimstore.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public class CCJContent {

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

    public CCJContent(Claim claim, InterestCalculationService interestCalculationService) {
        requireNonNull(claim);

        this.claimReferenceNumber = claim.getReferenceNumber();
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();
        TheirDetails defendant = countyCourtJudgment.getDefendant();
        this.defendantName = defendant.getName();
        this.defendantAddress = defendant.getAddress();
        this.repaymentOption = countyCourtJudgment.getPaymentOption().name();
        this.defendantEmail = defendant.getEmail().orElse(null);
        setAmountToPayByDefendant(interestCalculationService, claim);
        if (!countyCourtJudgment.getRepaymentPlan().isPresent()) {
            ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
            countyCourtJudgment.getPaidAmount();
            countyCourtJudgment.getPaymentOption();
        }
        this.claimantName = claim.getClaimData().getClaimant().getName();
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

    private void setAmountToPayByDefendant(
        final InterestCalculationService interestCalculationService,
        final Claim claim
    ) {
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();
        if (countyCourtJudgment.getPaymentOption() == PaymentOption.INSTALMENTS) {
            countyCourtJudgment.getRepaymentPlan().ifPresent(
                repaymentPlan -> this.amountToPayByDefendant = formatMoney(repaymentPlan.getRemainingAmount())
            );
        } else if (countyCourtJudgment.getPaymentOption() == PaymentOption.FULL_BY_SPECIFIED_DATE) {
            this.amountToPayByDefendant = calculateClaimAmountWithInterest(
                interestCalculationService,
                claim,
                countyCourtJudgment.getPayBySetDate().orElse(null)
            );
        } else if (countyCourtJudgment.getPaymentOption() == PaymentOption.IMMEDIATELY) {
            this.amountToPayByDefendant = calculateClaimAmountWithInterest(
                interestCalculationService,
                claim,
                claim.getCountyCourtJudgmentRequestedAt().toLocalDate()
            );
        }
    }

    private String calculateClaimAmountWithInterest(
        final InterestCalculationService interestCalculationService,
        final Claim claim,
        final LocalDate toDate
    ) {
        BigDecimal claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
        BigDecimal paidAmount = claim.getCountyCourtJudgment().getPaidAmount();

        if (!claim.getClaimData().getInterest().getType().equals(Interest.InterestType.NO_INTEREST)) {
            BigDecimal interestRate = claim.getClaimData().getInterest().getRate();
            BigDecimal interestAmount = interestCalculationService.calculateInterest(
                claimAmount.subtract(paidAmount),
                interestRate,
                getInterestFromDate(claim),
                toDate
            );
            return formatMoney(claimAmount.add(interestAmount).subtract(paidAmount));
        }
        return formatMoney(claimAmount.subtract(paidAmount));
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
