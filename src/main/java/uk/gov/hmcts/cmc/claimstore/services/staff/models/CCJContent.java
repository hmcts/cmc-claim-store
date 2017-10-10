package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;

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
    private String paymentRepaymentOption;
    private String signerName;
    private String signerRole;

    public CCJContent(Claim claim) {
        requireNonNull(claim);

        this.claimReferenceNumber = claim.getReferenceNumber();
        TheirDetails defendant = claim.getClaimData().getDefendant();
        this.defendantName = defendant.getName();
        this.defendantAddress = defendant.getAddress();
        this.paymentRepaymentOption = claim.getCountyCourtJudgment().getPaymentOption().name();
        this.defendantEmail = defendant.getEmail().orElse(null);
        this.amountToPayByDefendant = formatMoney(
            ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount()
        );
        this.claimantName = claim.getClaimData().getClaimant().getName();
        this.requestedDate = Formatting.formatDate(claim.getCountyCourtJudgmentRequestedAt());

        claim.getClaimData().getStatementOfTruth().ifPresent(sot -> {
            this.signerName = sot.getSignerName();
            this.signerRole = sot.getSignerRole();
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

    public String getPaymentRepaymentOption() {
        return paymentRepaymentOption;
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
}
