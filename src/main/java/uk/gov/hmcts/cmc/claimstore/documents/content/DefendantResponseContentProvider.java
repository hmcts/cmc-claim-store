package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.util.HashMap;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;

@Component
public class DefendantResponseContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimDataContentProvider claimDataContentProvider;
    private final NotificationsProperties notificationsProperties;

    public DefendantResponseContentProvider(
        final PartyDetailsContentProvider partyDetailsContentProvider,
        final ClaimDataContentProvider claimDataContentProvider,
        final NotificationsProperties notificationsProperties) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
        this.claimDataContentProvider = claimDataContentProvider;
        this.notificationsProperties = notificationsProperties;
    }

    public Map<String, Object> createContent(final Claim claim) {
        requireNonNull(claim);
        Response defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);

        Map<String, Object> content = new HashMap<>();
        Optional<StatementOfTruth> optionalStatementOfTruth = defendantResponse.getStatementOfTruth();
        content.put("signerName", optionalStatementOfTruth.map((StatementOfTruth::getSignerName)).orElse(null));
        content.put("signerRole", optionalStatementOfTruth.map((StatementOfTruth::getSignerRole)).orElse(null));

        content.put("claim", claimDataContentProvider.createContent(claim));
        content.put("defenceSubmittedOn", formatDateTime(claim.getRespondedAt()));
        content.put("responseDefence", defendantResponse.getDefence());
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
        return content;
    }
}
