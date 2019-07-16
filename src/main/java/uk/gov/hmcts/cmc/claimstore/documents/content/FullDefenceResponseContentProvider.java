package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.DirectionsQuestionnaireContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.EvidenceContent;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.ParagraphEnumerator.split;

@Component
public class FullDefenceResponseContentProvider {

    private static final String DEFENCE_FORM_NO = "OCON9B";
    private final DirectionsQuestionnaireContentProvider hearingContentProvider;

    @Autowired
    public FullDefenceResponseContentProvider(DirectionsQuestionnaireContentProvider hearingContentProvider) {
        this.hearingContentProvider = hearingContentProvider;
    }


    public Map<String, Object> createContent(FullDefenceResponse fullDefenceResponse) {
        requireNonNull(fullDefenceResponse);
        Map<String, Object> content = new HashMap<>();

        List<TimelineEvent> events = null;
        List<EvidenceContent> evidences = null;
        String timelineComment = null;
        String evidenceComment = null;

        content.put("responseDefence", split(fullDefenceResponse.getDefence().orElse("")));
        content.put("responseTypeSelected", fullDefenceResponse.getDefenceType().getDescription());
        if (fullDefenceResponse.getDefenceType().equals(DefenceType.ALREADY_PAID)) {
            content.put("hasDefendantAlreadyPaid", true);
        }

        fullDefenceResponse.getPaymentDeclaration().ifPresent(paymentDeclaration ->
            content.put("paymentDeclaration", createContentFor(paymentDeclaration))
        );

        Optional<DefendantTimeline> defenceTimeline = fullDefenceResponse.getTimeline();
        if (defenceTimeline.isPresent()) {
            DefendantTimeline defendantTimeline = defenceTimeline.get();
            events = defendantTimeline.getEvents();
            timelineComment = defendantTimeline.getComment().orElse(null);
        }

        Optional<DefendantEvidence> defenceEvidence = fullDefenceResponse.getEvidence();
        if (defenceEvidence.isPresent()) {
            DefendantEvidence defendantEvidence = defenceEvidence.get();
            evidences = Optional.ofNullable(defendantEvidence.getRows())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(Objects::nonNull)
                .map(e -> new EvidenceContent(e.getType().getDescription(), e.getDescription().orElse(null)))
                .collect(Collectors.toList());
            evidenceComment = defendantEvidence.getComment().orElse(null);
        }

        content.put("events", events);
        content.put("timelineComment", timelineComment);
        content.put("evidences", evidences);
        content.put("evidenceComment", evidenceComment);
        content.put("formNumber", DEFENCE_FORM_NO);

        content.put("hearingContent", fullDefenceResponse.getDirectionsQuestionnaire()
            .map(DirectionsQuestionnaireContentProvider::));
        return content;
    }

    private Map<Object, Object> createContentFor(PaymentDeclaration paymentDeclaration) {
        return ImmutableMap.builder()
            .put("paidDate", formatDate(paymentDeclaration.getPaidDate()))
            .put("explanation", paymentDeclaration.getExplanation())
            .build();
    }
}
