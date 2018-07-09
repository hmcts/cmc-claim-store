package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.EvidenceContent;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class PartAdmissionResponseContentProvider {

    private final AdmissionContentProvider admissionContentProvider;

    public PartAdmissionResponseContentProvider(
        AdmissionContentProvider admissionContentProvider
    ) {
        this.admissionContentProvider = admissionContentProvider;
    }

    public Map<String, Object> createContent(PartAdmissionResponse partAdmissionResponse) {
        List<TimelineEvent> events = null;
        List<EvidenceContent> evidences = null;
        String timelineComment = null;
        String evidenceComment = null;

        ImmutableMap.Builder<String, Object> content = new ImmutableMap.Builder<String, Object>()
            .put("responseDefence", partAdmissionResponse.getDefence())
            .put("responseTypeSelected", partAdmissionResponse.getResponseType().getDescription())
            .put("isAlreadyPaid", partAdmissionResponse.getIsAlreadyPaid())
            .put("paidAmount", formatMoney(partAdmissionResponse.getPaymentDetails().getAmount()));

        partAdmissionResponse.getPaymentDetails().getDate()
            .ifPresent(date -> content.put("paymentDate", formatDate(date)));
        partAdmissionResponse.getPaymentDetails().getPaymentMethod()
            .ifPresent(method -> content.put("paymentMethod", method));


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

        content.put("events", events)
            .put("timelineComment", timelineComment)
            .put("evidences", evidences)
            .put("evidenceComment", evidenceComment);

        partAdmissionResponse.getPaymentOption().ifPresent(
            paymentOption -> content.putAll(admissionContentProvider.createPaymentPlanDetails(
                paymentOption,
                partAdmissionResponse.getResponseType(),
                partAdmissionResponse.getPaymentDate().orElse(null),
                partAdmissionResponse.getRepaymentPlan().orElse(null))
            )
        );

        partAdmissionResponse.getStatementOfMeans().ifPresent(
            statementOfMeans -> content.putAll(admissionContentProvider.createStatementOfMeansContent(statementOfMeans))
        );

        return content.build();
    }

}
