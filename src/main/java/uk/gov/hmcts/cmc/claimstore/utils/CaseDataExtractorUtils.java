package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class CaseDataExtractorUtils {

    private CaseDataExtractorUtils(){
        // NO-OP
    }

    public static List<String> getDefendant(Claim claim) {
        return List
            .of(requireNonNull(claim
                .getClaimData()
                .getDefendant()
                .getName()
            ));
    }
}
