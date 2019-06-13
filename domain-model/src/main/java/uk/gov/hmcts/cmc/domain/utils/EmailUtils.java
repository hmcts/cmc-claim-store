package uk.gov.hmcts.cmc.domain.utils;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

public final class EmailUtils {

    private EmailUtils() {
        // utility class, no instances
    }

    public static Optional<String> getDefendantEmail(Claim claim) {
        if (StringUtils.isNotBlank(claim.getDefendantEmail())) {
            return Optional.of(claim.getDefendantEmail());
        } else {
            return claim.getClaimData().getDefendant().getEmail();
        }
    }
}
