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

import static uk.gov.hmcts.cmc.claimstore.documents.content.FullAdmissionResponseContentProvider.ADMISSIONS_FORM_NO;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class PartAdmissionResponseContentProvider {

    private final PaymentIntentionContentProvider paymentIntentionContentProvider;
    private final StatementOfMeansContentProvider statementOfMeansContentProvider;

    public PartAdmissionResponseContentProvider(
        PaymentIntentionContentProvider paymentIntentionContentProvider,
        StatementOfMeansContentProvider statementOfMeansContentProvider
    ) {
        this.paymentIntentionContentProvider = paymentIntentionContentProvider;
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

        content.put("amount", formatMoney(partAdmissionResponse.getAmount()));

        partAdmissionResponse.getPaymentDeclaration()
            .ifPresent(paymentDeclaration -> {
                content.put("paymentDate", formatDate(paymentDeclaration.getPaidDate()));
                content.put("paymentMethod", paymentDeclaration.getExplanation());
            });

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

        partAdmissionResponse.getPaymentIntention().ifPresent(
            paymentIntention ->
                content.putAll(paymentIntentionContentProvider.createContent(
                    paymentIntention.getPaymentOption(),
                    paymentIntention.getRepaymentPlan().orElse(null),
                    paymentIntention.getPaymentDate().orElse(null),
                    formatMoney(partAdmissionResponse.getAmount())
                    )
                )
        );

        partAdmissionResponse.getStatementOfMeans().ifPresent(
            statementOfMeans -> content.putAll(statementOfMeansContentProvider.createContent(statementOfMeans))
        );

        content.put("formNumber", ADMISSIONS_FORM_NO);

        return content;
    }
}
