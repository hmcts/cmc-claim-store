package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.MediationOutcome;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static uk.gov.hmcts.cmc.domain.models.MediationOutcome.FAILED;
import static uk.gov.hmcts.cmc.domain.models.MediationOutcome.SUCCEEDED;

public class MapperUtil {
    private static final String OTHERS = " + others";

    public static final Function<Claim, String> toCaseName = claim ->
        fetchClaimantName(claim) + " Vs " + fetchDefendantName(claim);

    private MapperUtil() {
        // Utility class, no instances
    }

    public static MediationOutcome getMediationOutcome(CCDCase ccdCase) {

        CCDRespondent defendant = ccdCase.getRespondents()
            .stream().findFirst().map(CCDCollectionElement::getValue).orElseThrow(IllegalStateException::new);

        if (Optional.ofNullable(defendant.getMediationFailedReason()).isPresent()) {
            return FAILED;
        } else if (Optional.ofNullable(defendant.getMediationSettlementReachedAt()).isPresent()) {
            return SUCCEEDED;
        } else {
            return null;
        }
    }

    public static boolean isAnyNotNull(Object... objects) {
        return Stream.of(objects).anyMatch(Objects::nonNull);
    }

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
