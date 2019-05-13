package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MapperUtil {
    private static final String OTHERS = " + others";

    public static final Function<Claim, String> toCaseName = claim ->
        fetchClaimantName(claim) + " Vs " + fetchDefendantName(claim);

    private MapperUtil() {
        // Utility class, no instances
    }

    public static boolean isAnyNotNull(Object... objects) {
        return Stream.of(objects).anyMatch(Objects::nonNull);
    }

    public static Function<CCDClaimSubmissionOperationIndicators, ClaimSubmissionOperationIndicators>
        mapClaimSubmissionOperationIndicators = claimSubmissionOperationIndicators ->
        ClaimSubmissionOperationIndicators.builder()
            .claimantNotification(YesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .defendantNotification(YesNoOption.valueOf(claimSubmissionOperationIndicators.getDefendantNotification().name()))
            .bulkPrint(YesNoOption.valueOf(claimSubmissionOperationIndicators.getBulkPrint().name()))
            .rpa(YesNoOption.valueOf(claimSubmissionOperationIndicators.getRPA().name()))
            .staffNotification(YesNoOption.valueOf(claimSubmissionOperationIndicators.getStaffNotification().name()))
            .sealedClaimUpload(YesNoOption.valueOf(claimSubmissionOperationIndicators.getSealedClaimUpload().name()))
            .claimIssueReceiptUpload(YesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimIssueReceiptUpload().name()))
            .defendantPinLetterUpload(YesNoOption.valueOf(claimSubmissionOperationIndicators.getDefendantPinLetterUpload().name()))
            .build();

    public static Supplier<ClaimSubmissionOperationIndicators> getDefaultClaimSubmissionOperationIndicators =
        () -> ClaimSubmissionOperationIndicators.builder()
            .claimantNotification(YesNoOption.NO)
            .defendantNotification(YesNoOption.NO)
            .bulkPrint(YesNoOption.NO)
            .rpa(YesNoOption.NO)
            .staffNotification(YesNoOption.NO)
            .sealedClaimUpload(YesNoOption.NO)
            .claimIssueReceiptUpload(YesNoOption.NO)
            .defendantPinLetterUpload(YesNoOption.NO)
            .build();

    private static String fetchDefendantName(Claim claim) {
        StringBuilder defendantNameBuilder = new StringBuilder();

        defendantNameBuilder.append(claim.getResponse().map(Response::getDefendant)
            .map(Party::getName)
            .orElseGet(() -> claim.getClaimData().getDefendants().get(0).getName()));

        if (claim.getClaimData().getDefendants().size() > 1) {
            defendantNameBuilder.append(OTHERS);
        }

        return defendantNameBuilder.toString();

    }

    private static String fetchClaimantName(Claim claim) {
        StringBuilder claimantNameBuilder = new StringBuilder();

        claimantNameBuilder.append(claim.getClaimData().getClaimants().get(0).getName());
        if (claim.getClaimData().getClaimants().size() > 1) {
            claimantNameBuilder.append(OTHERS);
        }
        return claimantNameBuilder.toString();
    }
}
