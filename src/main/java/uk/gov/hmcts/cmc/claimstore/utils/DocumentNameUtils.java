package uk.gov.hmcts.cmc.claimstore.utils;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.claimstore.utils.Preconditions.requireNonBlank;

public class DocumentNameUtils {

    private DocumentNameUtils() {
    }

    public static String buildSealedClaimFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-claim-form", number);
    }

    public static String buildJsonClaimFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-json-claim", number);
    }

    public static String buildJsonRequestForJudgementFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-json-request-for-judgement", number);
    }

    public static String buildJsonMoreTimeRequestedFileBaseName(String number) {
        requireNonBlank(number);

        return format("%s-json-more-time-requested", number);
    }

    public static String buildJsonDefenceResponseFileBaseName(String number) {
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

}
