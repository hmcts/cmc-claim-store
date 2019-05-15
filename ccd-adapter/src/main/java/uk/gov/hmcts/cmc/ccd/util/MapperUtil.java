package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
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
        mapFromCCDClaimSubmissionOperationIndicators = claimSubmissionOperationIndicators ->
        ClaimSubmissionOperationIndicators.builder()
            .claimantNotification(YesNoOption.fromValue(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .defendantNotification(YesNoOption.fromValue(claimSubmissionOperationIndicators.getDefendantNotification().name()))
            .bulkPrint(YesNoOption.fromValue(claimSubmissionOperationIndicators.getBulkPrint().name()))
            .rpa(YesNoOption.fromValue(claimSubmissionOperationIndicators.getRpa().name()))
            .staffNotification(YesNoOption.fromValue(claimSubmissionOperationIndicators.getStaffNotification().name()))
            .sealedClaimUpload(YesNoOption.fromValue(claimSubmissionOperationIndicators.getSealedClaimUpload().name()))
            .claimIssueReceiptUpload(YesNoOption.fromValue(claimSubmissionOperationIndicators.getClaimIssueReceiptUpload().name()))
            .build();

    public static Function<ClaimSubmissionOperationIndicators, CCDClaimSubmissionOperationIndicators>
        mapClaimSubmissionOperationIndicatorsToCCD = claimSubmissionOperationIndicators ->
        CCDClaimSubmissionOperationIndicators.builder()
            .claimantNotification(CCDYesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimantNotification().name()))
            .defendantNotification(CCDYesNoOption.valueOf(claimSubmissionOperationIndicators.getDefendantNotification().name()))
            .bulkPrint(CCDYesNoOption.valueOf(claimSubmissionOperationIndicators.getBulkPrint().name()))
            .rpa(CCDYesNoOption.valueOf(claimSubmissionOperationIndicators.getRpa().name()))
            .staffNotification(CCDYesNoOption.valueOf(claimSubmissionOperationIndicators.getStaffNotification().name()))
            .sealedClaimUpload(CCDYesNoOption.valueOf(claimSubmissionOperationIndicators.getSealedClaimUpload().name()))
            .claimIssueReceiptUpload(CCDYesNoOption.valueOf(claimSubmissionOperationIndicators.getClaimIssueReceiptUpload().name()))
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
