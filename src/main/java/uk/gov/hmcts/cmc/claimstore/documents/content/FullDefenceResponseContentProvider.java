package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.HearingContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.EvidenceContent;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

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
    private final HearingContentProvider hearingContentProvider;

    @Autowired
    public FullDefenceResponseContentProvider(HearingContentProvider hearingContentProvider) {
        this.hearingContentProvider = hearingContentProvider;
    }

    public Map<String, Object> createContent(FullDefenceResponse fullDefenceResponse) {
        requireNonNull(fullDefenceResponse);
        Map<String, Object> content = new HashMap<>();

        content.put("responseDefence", split(fullDefenceResponse.getDefence().orElse("")));
        content.put("responseTypeSelected", fullDefenceResponse.getDefenceType().getDescription());
        if (DefenceType.ALREADY_PAID.equals(fullDefenceResponse.getDefenceType())) {
            content.put("hasDefendantAlreadyPaid", true);
            fullDefenceResponse.getPaymentDeclaration()
                .flatMap(PaymentDeclaration::getPaidAmount)
                .map(Formatting::formatMoney)
                .ifPresent(amount -> content.put("paidAmount", amount));
        }

        fullDefenceResponse.getPaymentDeclaration()
            .ifPresent(paymentDeclaration -> {
                content.put("paymentDate", formatDate(paymentDeclaration.getPaidDate()));
                content.put("paymentMethod", paymentDeclaration.getExplanation());
            });

        fullDefenceResponse.getFreeMediation().ifPresent(mediation ->
            content.put("mediation", mediation.equals(YesNoOption.YES))
        );

        Optional<DefendantTimeline> defenceTimeline = fullDefenceResponse.getTimeline();
        List<TimelineEvent> events = defenceTimeline.map(DefendantTimeline::getEvents).orElse(null);
        String timelineComment = defenceTimeline.flatMap(DefendantTimeline::getComment).orElse(null);

        Optional<DefendantEvidence> defenceEvidence = fullDefenceResponse.getEvidence();
        List<EvidenceContent> evidences = defenceEvidence.map(DefendantEvidence::getRows)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(Objects::nonNull)
            .map(this::createContentFor)
            .collect(Collectors.toList());
        String evidenceComment = defenceEvidence.flatMap(DefendantEvidence::getComment).orElse(null);

        content.put("events", events);
        content.put("timelineComment", timelineComment);
        content.put("evidences", evidences);
        content.put("evidenceComment", evidenceComment);
        content.put("formNumber", DEFENCE_FORM_NO);

        fullDefenceResponse.getDirectionsQuestionnaire().ifPresent(dq ->
            content.put("hearingContent", hearingContentProvider.mapDirectionQuestionnaire(dq)));

        return content;
    }

    private EvidenceContent createContentFor(EvidenceRow evidenceRow) {
        return new EvidenceContent(evidenceRow.getType().getDescription(), evidenceRow.getDescription().orElse(null));
    }
}
