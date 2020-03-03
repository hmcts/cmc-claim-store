package uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;

@Component
public class ClaimantDirectionsQuestionnaireContentProvider {

    private final ClaimContentProvider claimContentProvider;
    private final HearingContentProvider hearingContentProvider;

    public ClaimantDirectionsQuestionnaireContentProvider(ClaimContentProvider claimContentProvider,
                                                          HearingContentProvider hearingContentProvider) {
        this.claimContentProvider = claimContentProvider;
        this.hearingContentProvider = hearingContentProvider;
    }

    public Map<String, Object> createContent(Claim claim) {
        DirectionsQuestionnaire claimantDirectionsQuestionnaire = claim.getClaimantResponse()
            .filter(ResponseRejection.class::isInstance)
            .map(ResponseRejection.class::cast)
            .flatMap(ResponseRejection::getDirectionsQuestionnaire)
            .orElseThrow(() -> new IllegalArgumentException("Missing directions questionnaire"));

        ImmutableMap.Builder<String, Object> contentBuilder = new ImmutableMap.Builder<>();
        contentBuilder.putAll(claimContentProvider.createContent(claim));
        contentBuilder.put("hearingContent", hearingContentProvider
            .mapDirectionQuestionnaire(claimantDirectionsQuestionnaire));
        claim.getClaimantRespondedAt().ifPresent(respondedAt ->
            contentBuilder.put("claimantSubmittedOn", formatDateTime(respondedAt))
        );
        contentBuilder.put("formNumber", "OCON180");
        return contentBuilder.build();
    }
}
