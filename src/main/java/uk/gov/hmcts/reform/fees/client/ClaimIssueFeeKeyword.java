package uk.gov.hmcts.reform.fees.client;

import java.math.BigDecimal;

enum ClaimIssueFeeKeyword {
    PaperClaimUpTo300,
    PaperClaimUpTo500,
    PaperClaimUpTo1000,
    PaperClaimUpTo1500,
    PaperClaimUpTo3k,
    PaperClaimUpTo5k,
    PaperClaimUpTo10k,
    PaperClaimUpTo200k,
    PaperClaimAbove200k,
    UnspecifiedClaim;

    public static String getKeywordIssueEvent(BigDecimal amount) {

        if (amount.compareTo(BigDecimal.valueOf(0.01)) >= 0 && amount.compareTo(BigDecimal.valueOf(300)) <= 0) {
            return PaperClaimUpTo300.name();
        } else if (amount.compareTo(BigDecimal.valueOf(300.01)) >= 0 && amount.compareTo(BigDecimal.valueOf(500)) <= 0) {
            return PaperClaimUpTo5k.name();
        } else if (amount.compareTo(BigDecimal.valueOf(500.01)) >= 0 && amount.compareTo(BigDecimal.valueOf(1000)) <= 0) {
            return PaperClaimUpTo1000.name();
        } else if (amount.compareTo(BigDecimal.valueOf(1000.01)) >= 0 && amount.compareTo(BigDecimal.valueOf(1500)) <= 0) {
            return PaperClaimUpTo1500.name();
        } else if (amount.compareTo(BigDecimal.valueOf(1500.01)) >= 0 && amount.compareTo(BigDecimal.valueOf(3000)) <= 0) {
            return PaperClaimUpTo3k.name();
        } else if (amount.compareTo(BigDecimal.valueOf(3000.01)) >= 0 && amount.compareTo(BigDecimal.valueOf(5000)) <= 0) {
            return PaperClaimUpTo5k.name();
        } else if (amount.compareTo(BigDecimal.valueOf(5000.01)) >= 0 && amount.compareTo(BigDecimal.valueOf(10000)) <= 0) {
            return PaperClaimUpTo10k.name();
        } else if (amount.compareTo(BigDecimal.valueOf(10000.01)) >= 0 && amount.compareTo(BigDecimal.valueOf(200000)) <= 0) {
            return PaperClaimUpTo200k.name();
        } else if (amount.compareTo(BigDecimal.valueOf(200000.01)) >= 0) {
            return PaperClaimAbove200k.name();
        } else {
            return UnspecifiedClaim.name();
        }
    }
}
