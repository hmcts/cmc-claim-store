package uk.gov.hmcts.cmc.claimstore.utils;

import static java.lang.String.format;

public class DocumentNameUtils {

    private DocumentNameUtils() {
    }

    public static String buildSealedClaimFilename(String number) {
        return format("%s-sealed-claim", number);
    }

}
