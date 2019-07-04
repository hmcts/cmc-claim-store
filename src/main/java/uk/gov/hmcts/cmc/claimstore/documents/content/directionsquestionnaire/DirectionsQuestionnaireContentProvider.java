package uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DirectionsQuestionnaireContentProvider {

    private final ClaimantContentProvider claimantContentProvider;

    @Autowired
    public DirectionsQuestionnaireContentProvider(ClaimantContentProvider claimantContentProvider) {
        this.claimantContentProvider = claimantContentProvider;

    }

    public Map<String, Object> getClaimantContent(Claim claim){
        Map<String, Object> contentDetails = new HashMap<>();
        contentDetails.put("claimant",  claimantContentProvider.createContent(
            claim.getClaimData().getClaimant(),
            claim.getSubmitterEmail())
        );

        return contentDetails;
    }


    public Function<Claim, Map<String, String>> getDefendantContent = directionQuestionnaire -> Collections.EMPTY_MAP;

}
