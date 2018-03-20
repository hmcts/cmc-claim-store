package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.EvidenceContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

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
        content.put("freeMediation", defendantResponse.getFreeMediation()
            .orElse(Response.FreeMediationOption.NO)
            .name()
            .toLowerCase());
        content.put("responseDashboardUrl", notificationsProperties.getFrontendBaseUrl());

        content.put("defendant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            defendantResponse.getDefendant(),
            claim.getDefendantEmail()
        ));
        content.put("claimant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getClaimant(),
            claim.getSubmitterEmail()
        ));

        if (defendantResponse instanceof FullDefenceResponse) {
            FullDefenceResponse fullDefence = (FullDefenceResponse) defendantResponse;

            content.put("responseDefence", fullDefence.getDefence());

            fullDefence.getPaymentDeclaration().ifPresent(paymentDeclaration ->
                content.put("paymentDeclaration", createContentFor(paymentDeclaration))
            );

            if (fullDefence.getTimeline().isPresent()) {
                DefendantTimeline defendantTimeline = fullDefence.getTimeline().get();
                content.put("events", defendantTimeline.getEvents());
                content.put("timelineComment", defendantTimeline.getComment().orElse(null));
            }

            if (fullDefence.getEvidence().isPresent()) {
                DefendantEvidence defendantEvidence = fullDefence.getEvidence().get();
                List<EvidenceContent> evidences = Optional.ofNullable(defendantEvidence.getRows())
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(e -> new EvidenceContent(e.getType().getDescription(), e.getDescription().orElse(null)))
                    .collect(Collectors.toList());
                content.put("evidences", evidences);
                content.put("evidenceComment", defendantEvidence.getComment().orElse(null));
            }
        }

        return content;
    }

    private Map<Object, Object> createContentFor(PaymentDeclaration paymentDeclaration) {
        return ImmutableMap.builder()
            .put("paidDate", formatDate(paymentDeclaration.getPaidDate()))
            .put("explanation", paymentDeclaration.getExplanation())
            .build();
    }
}
