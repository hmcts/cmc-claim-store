package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class MapperUtil {

    private MapperUtil() {
        // Utility class, no instances
    }

    public static boolean isAllNull(Object... objects) {
        return Stream.of(objects).allMatch(Objects::nonNull);
    }

    public static boolean isAnyNotNull(Object... objects) {
        return Stream.of(objects).anyMatch(Objects::nonNull);
    }

    public static Function<CCDClaimSubmissionOperationIndicators, ClaimSubmissionOperationIndicators>
        mapClaimSubmissionOperationIndicators = claimSubmissionOperationIndicators ->
        ClaimSubmissionOperationIndicators.builder()
            .claimantNotification(YesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .defendantNotification(YesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .bulkPrint(YesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .RPA(YesNoOption.valueOf(claimSubmissionOperationIndicators.getRPA().name()))
            .staffNotification(YesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .sealedClaimUpload(YesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .claimIssueReceiptUpload(YesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .defendantPinLetterUpload(YesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .build();
}
