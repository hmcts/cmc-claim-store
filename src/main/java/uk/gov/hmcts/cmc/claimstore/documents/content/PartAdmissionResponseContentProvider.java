package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.EvidenceContent;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PartAdmissionResponseContentProvider {

    private final StatementOfMeansContentProvider statementOfMeansContentProvider;

    public PartAdmissionResponseContentProvider(
        StatementOfMeansContentProvider statementOfMeansContentProvider
    ) {
        this.statementOfMeansContentProvider = statementOfMeansContentProvider;
    }

    public Map<String, Object> createContent(PartAdmissionResponse partAdmissionResponse) {
        Map<String, Object> content = new HashMap<>();

        List<TimelineEvent> events = null;
        List<EvidenceContent> evidences = null;
        String timelineComment = null;
        String evidenceComment = null;

        content.put("responseDefence", partAdmissionResponse.getDefence());
        content.put("responseTypeSelected", partAdmissionResponse.getResponseType().getDescription());

        content.put("isAlreadyPaid", partAdmissionResponse.getIsAlreadyPaid());


        Optional<DefendantTimeline> defenceTimeline = partAdmissionResponse.getTimeline();
        if (defenceTimeline.isPresent()) {
            DefendantTimeline defendantTimeline = defenceTimeline.get();
            events = defendantTimeline.getEvents();
            timelineComment = defendantTimeline.getComment().orElse(null);
        }

        Optional<DefendantEvidence> defenceEvidence = partAdmissionResponse.getEvidence();
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

        return content;
    }
}
