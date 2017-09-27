package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.claimstore.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.utils.PartyUtils;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public class CCJContent {

    String claimReferenceNumber;
    String claimantName;
    String claimantType;
    Address claimantAddress;

    String defendantName;
    Address defendantAddress;

    String issueDate;
    String ccjRequestAt;
    String paymentRepaymentOption;
    String paymentByDate;
    String requestedPaymentDate;

    String instalmentsAmount;
    String firstPaymentDate;
    String claimFees;
    String claimAmount;
    InterestContent interest;
    String totalAmountWithInterest;


    public CCJContent(
        Claim claim,
        InterestContent interest,
        String totalAmountWithInterest
    ) {
        requireNonNull(claim);
        requireNonNull(interest);
        requireNonNull(totalAmountWithInterest);

        this.claimReferenceNumber = claim.getReferenceNumber();
        Party claimant = claim.getClaimData().getClaimant();
        this.claimantName = claimant.getName();
        this.claimantType = PartyUtils.getType(claimant);
        this.claimantAddress = claimant.getAddress();

        TheirDetails defendant = claim.getClaimData().getDefendant();
        this.defendantName = defendant.getName();
        this.defendantAddress = defendant.getAddress();

        this.issueDate = formatDate(claim.getCountyCourtJudgmentRequestedAt());
        this.ccjRequestAt = formatDate(claim.getCountyCourtJudgmentRequestedAt());
        this.paymentRepaymentOption = claim.getCountyCourtJudgment().getPaymentOption().name();
        this.claimFees = formatMoney(claim.getClaimData().getFeesPaidInPound());
        this.claimAmount = formatMoney(((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount());

        this.interest = interest;
        this.totalAmountWithInterest = totalAmountWithInterest;

        if (claim.getCountyCourtJudgment().getPaymentOption() == PaymentOption.IMMEDIATELY) {
            LocalDateTime ccjRequestAt = claim.getCountyCourtJudgmentRequestedAt();
            LocalDateTime adjustedDate = ccjRequestAt.plusDays(28);
            this.paymentByDate = formatDate(adjustedDate);

        } else if (claim.getCountyCourtJudgment().getPaymentOption() == PaymentOption.INSTALMENTS) {
            claim.getCountyCourtJudgment().getRepaymentPlan().ifPresent(rp -> {
                this.instalmentsAmount = formatMoney(rp.getInstalmentAmount());
                this.firstPaymentDate = formatDate(rp.getFirstPaymentDate());
            });
        } else if (claim.getCountyCourtJudgment().getPaymentOption() == PaymentOption.FULL_BY_SPECIFIED_DATE) {
            this.requestedPaymentDate = formatDate(claim.getCountyCourtJudgmentRequestedAt());
        }
    }

    public String getClaimantName() {
        return claimantName;
    }

    public Address getClaimantAddress() {
        return claimantAddress;
    }
}
