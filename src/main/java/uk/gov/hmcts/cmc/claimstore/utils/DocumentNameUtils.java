package uk.gov.hmcts.cmc.claimstore.utils;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.claimstore.utils.Preconditions.requireNonBlank;

public class DocumentNameUtils {

    public static final String JSON_EXTENSION = ".json";

    private DocumentNameUtils() {
    }

    public static String buildSealedClaimFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-claim-form", number);
    }

    public static String buildClaimIssueReceiptFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-claim-form-claimant-copy", number);
    }

    public static String buildReviewOrderFileBaseName(String caseRef) {
        requireNonBlank(caseRef);

        return format("%s-review-order", caseRef);
    }

    public static String buildJsonClaimFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-json-claim", number);
    }

    public static String buildRequestForJudgementFileBaseName(String caseRef, String partyName) {
        requireNonBlank(caseRef);
        requireNonBlank(partyName);

        return format("%s-%s-county-court-judgment-details", caseRef, partyName);
    }

    public static String buildJsonRequestForJudgementFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-json-request-for-judgement", number);
    }

    public static String buildJsonMoreTimeRequestedFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-json-more-time-requested", number);
    }

    public static String buildJsonPaidInFullFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-json-paid-in-full", number);
    }

    public static String buildResponseFileBaseName(String caseRef) {
        requireNonBlank(caseRef);

        return format("%s-claim-response", caseRef);
    }

    public static String buildClaimantResponseFileBaseName(String caseRef) {
        requireNonBlank(caseRef);

        return format("%s-claimant-response", caseRef);
    }

    public static String buildClaimantHearingFileBaseName(String caseRef) {
        requireNonBlank(caseRef);

        return format("%s-claimant-hearing-questions", caseRef);
    }

    public static String buildJsonResponseFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-json-defence-response", number);
    }

    public static boolean isSealedClaim(String filename) {
        requireNonBlank(filename);

        return filename.contains("claim-form");
    }

    public static String buildDefendantLetterFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-defendant-pin-letter", number);
    }

    public static String buildSettlementReachedFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-settlement-agreement", number);
    }

    public static String buildDirectionsOrderFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-directions-order", number);
    }

    public static String buildCoverSheetFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-directions-order-cover-sheet", number);
    }

    public static String buildRequestForJudgmentByAdmissionOrDeterminationFileBaseName(String caseRef, String ccjType) {
        requireNonBlank(caseRef);
        requireNonBlank(ccjType);

        return format("%s-ccj-request-%s", caseRef, ccjType);
    }
}
