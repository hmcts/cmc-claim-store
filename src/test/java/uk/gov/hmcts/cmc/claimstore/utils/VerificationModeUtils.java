package uk.gov.hmcts.cmc.claimstore.utils;

import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.times;

public final class VerificationModeUtils {
    private VerificationModeUtils() {
        // NO-OP
    }

    public static VerificationMode once() {
        return times(1);
    }
}
