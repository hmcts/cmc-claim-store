package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ClaimantInvalidRepaymentPlanException extends RuntimeException {
    public ClaimantInvalidRepaymentPlanException(String message) {
        super(message);
    }
}
