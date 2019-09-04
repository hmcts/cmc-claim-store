package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.HearingContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.EvidenceContent;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

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
import static uk.gov.hmcts.cmc.claimstore.utils.ParagraphEnumerator.split;

@Component
public class PartAdmissionResponseContentProvider {

    private final PaymentIntentionContentProvider paymentIntentionContentProvider;
    private final StatementOfMeansContentProvider statementOfMeansContentProvider;
    private final HearingContentProvider hearingContentProvider;

    public PartAdmissionResponseContentProvider(
        PaymentIntentionContentProvider paymentIntentionContentProvider,
        StatementOfMeansContentProvider statementOfMeansContentProvider,
        HearingContentProvider hearingContentProvider
    ) {
        this.paymentIntentionContentProvider = paymentIntentionContentProvider;
        this.statementOfMeansContentProvider = statementOfMeansContentProvider;
        this.hearingContentProvider = hearingContentProvider;
    }

    public Map<String, Object> createContent(PartAdmissionResponse partAdmissionResponse) {

        Map<String, Object> content = new HashMap<>();

        List<TimelineEvent> events = null;
        List<EvidenceContent> evidences = null;
        String timelineComment = null;
        String evidenceComment = null;

        content.put("responseDefence", split(partAdmissionResponse.getDefence()));
        content.put("responseTypeSelected", deriveResponseTypeSelected(partAdmissionResponse));

        content.put("amount", formatMoney(partAdmissionResponse.getAmount()));

        content.put("paymentDeclarationIsPresent", partAdmissionResponse.getPaymentDeclaration().isPresent());
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

        content.put("paymentIntentionIsPresent", partAdmissionResponse.getPaymentIntention().isPresent());
        partAdmissionResponse.getPaymentIntention().ifPresent(
            paymentIntention ->
                content.putAll(paymentIntentionContentProvider.createContent(
                    paymentIntention.getPaymentOption(),
                    paymentIntention.getRepaymentPlan().orElse(null),
                    paymentIntention.getPaymentDate().orElse(null),
                    formatMoney(partAdmissionResponse.getAmount()),
                    ""
                    )
                )
        );

        partAdmissionResponse.getFreeMediation().ifPresent(
            freeMediation -> content.put("mediation", freeMediation.equals(YesNoOption.YES))
        );

        partAdmissionResponse.getStatementOfMeans().ifPresent(
            statementOfMeans -> content.putAll(statementOfMeansContentProvider.createContent(statementOfMeans))
        );

        content.put("formNumber", ADMISSIONS_FORM_NO);
        partAdmissionResponse.getDirectionsQuestionnaire().ifPresent(dq ->
            content.put("hearingContent", hearingContentProvider.mapDirectionQuestionnaire(dq)));

        return content;
    }

    private String deriveResponseTypeSelected(PartAdmissionResponse response) {
        return response.getPaymentDeclaration().map(PaymentDeclaration::getPaidDate).isPresent()
            ? DefenceType.ALREADY_PAID.getDescription()
            : response.getResponseType().getDescription();
    }
}
