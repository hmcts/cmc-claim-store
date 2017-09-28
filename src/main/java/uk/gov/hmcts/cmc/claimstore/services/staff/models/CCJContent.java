package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public class CCJContent {

    String claimReferenceNumber;
    String defendantName;
    Address defendantAddress;
    String defendantEmail;
    String amountToPayByDefendant;
    String paymentRepaymentOption;

    public CCJContent( Claim claim ) {
        requireNonNull(claim);

        this.claimReferenceNumber = claim.getReferenceNumber();
        TheirDetails defendant = claim.getClaimData().getDefendant();
        this.defendantName = defendant.getName();
        this.defendantAddress = defendant.getAddress();
        this.paymentRepaymentOption = claim.getCountyCourtJudgment().getPaymentOption().name();
        this.defendantEmail = defendant.getEmail().orElse(null);
        this.amountToPayByDefendant = formatMoney(((AmountBreakDown)claim.getClaimData().getAmount()).getTotalAmount());
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

    public String getPaymentRepaymentOption() {
        return paymentRepaymentOption;
    }

    public String getAmountToPayByDefendant() {
        return amountToPayByDefendant;
    }
}
