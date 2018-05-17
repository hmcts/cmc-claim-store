package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.EvidenceContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
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
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;

@Component
public class DefendantResponseContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimDataContentProvider claimDataContentProvider;
    private final NotificationsProperties notificationsProperties;

    public DefendantResponseContentProvider(
        PartyDetailsContentProvider partyDetailsContentProvider,
        ClaimDataContentProvider claimDataContentProvider,
        NotificationsProperties notificationsProperties
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
        this.claimDataContentProvider = claimDataContentProvider;
        this.notificationsProperties = notificationsProperties;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);
        Response defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);

        Map<String, Object> content = new HashMap<>();
        Optional<StatementOfTruth> optionalStatementOfTruth = defendantResponse.getStatementOfTruth();
        content.put("signerName", optionalStatementOfTruth.map((StatementOfTruth::getSignerName)).orElse(null));
        content.put("signerRole", optionalStatementOfTruth.map((StatementOfTruth::getSignerRole)).orElse(null));

        content.put("claim", claimDataContentProvider.createContent(claim));
        content.put("defenceSubmittedOn", formatDateTime(claim.getRespondedAt()));
        content.put("defenceSubmittedDate", formatDate(claim.getRespondedAt()));
        content.put("freeMediation", defendantResponse.getFreeMediation()
            .orElse(YesNoOption.NO)
            .name()
            .toLowerCase());
        content.put("responseDashboardUrl", notificationsProperties.getFrontendBaseUrl());

        content.put("defendant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            defendantResponse.getDefendant(),
            claim.getDefendantEmail(),
            null,
            null
        ));
        content.put("claimant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getClaimant(),
            claim.getSubmitterEmail()
        ));

        List<TimelineEvent> events = null;
        List<EvidenceContent> evidences = null;
        String timelineComment = null;
        String evidenceComment = null;
        if (defendantResponse instanceof FullDefenceResponse) {
            FullDefenceResponse fullDefence = (FullDefenceResponse) defendantResponse;

            content.put("responseDefence", fullDefence.getDefence().orElse(null));
            content.put("responseTypeSelected", fullDefence.getDefenceType().getDescription());
            if (fullDefence.getDefenceType().equals(DefenceType.ALREADY_PAID)) {
                content.put("hasDefendantAlreadyPaid", true);
            }

            fullDefence.getPaymentDeclaration().ifPresent(paymentDeclaration ->
                content.put("paymentDeclaration", createContentFor(paymentDeclaration))
            );

            Optional<DefendantTimeline> defenceTimeline = fullDefence.getTimeline();
            if (defenceTimeline.isPresent()) {
                DefendantTimeline defendantTimeline = defenceTimeline.get();
                events = defendantTimeline.getEvents();
                timelineComment = defendantTimeline.getComment().orElse(null);
            }

            Optional<DefendantEvidence> defenceEvidence = fullDefence.getEvidence();
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
        }
        content.put("events", events);
        content.put("timelineComment", timelineComment);
        content.put("evidences", evidences);
        content.put("evidenceComment", evidenceComment);

        return content;
    }

    private Map<Object, Object> createContentFor(PaymentDeclaration paymentDeclaration) {
        return ImmutableMap.builder()
            .put("paidDate", formatDate(paymentDeclaration.getPaidDate()))
            .put("explanation", paymentDeclaration.getExplanation())
            .build();
    }
}
