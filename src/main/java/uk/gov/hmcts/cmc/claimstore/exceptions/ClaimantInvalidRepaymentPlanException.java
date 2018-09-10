package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ClaimantInvalidRepaymentPlanException extends RuntimeException {

    public static final String INVALID_RESPONSE_TYPE =
        "Defendant response must be part or full admission";
    public static final String INVALID_DEFENDANT_REPAYMENT_TYPE =
        "Expected defendant response to be installments or set date";
    public static final String EXPECTED_PAYMENT_DATE =
        "Expected payment date to be present on defendant response";
    public static final String EXPECTED_PAYMENT_INTENTION =
        "Expected payment intention to be present on defendant response";
    public static final String EXPECTED_REPAYMENT_PLAN =
        "Expected repayment plan to be present on defendant response";
    public static final String EXPECTED_DEFENDANT_RESPONSE =
        "Expected defendant response to be present";


    public ClaimantInvalidRepaymentPlanException(String message) {
        super(message);
    }
}
