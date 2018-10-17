package uk.gov.hmcts.cmc.claimstore.controllers;

public final class PathPatterns {

    public static final String UUID_PATTERN = "\\p{XDigit}{8}-\\p{XDigit}"
        + "{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}";

    public static final String CLAIM_REFERENCE_PATTERN = "^\\d{3}(?:LR|MC)\\d{3}$";

    private PathPatterns() {
        // NO-OP
    }

}
