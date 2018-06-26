package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;

@Component
public class DefendantResponseContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimDataContentProvider claimDataContentProvider;
    private final NotificationsProperties notificationsProperties;
    private final FullDefenceResponseContentProvider fullDefenceResponseContentProvider;
    private final FullAdmissionResponseContentProvider fullAdmissionResponseContentProvider;

    public DefendantResponseContentProvider(
        PartyDetailsContentProvider partyDetailsContentProvider,
        ClaimDataContentProvider claimDataContentProvider,
        NotificationsProperties notificationsProperties,
        FullDefenceResponseContentProvider fullDefenceResponseContentProvider,
        FullAdmissionResponseContentProvider fullAdmissionResponseContentProvider
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
        this.claimDataContentProvider = claimDataContentProvider;
        this.notificationsProperties = notificationsProperties;
        this.fullDefenceResponseContentProvider = fullDefenceResponseContentProvider;
        this.fullAdmissionResponseContentProvider = fullAdmissionResponseContentProvider;
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
        content.put("responseType", defendantResponse.getResponseType());

        if (defendantResponse instanceof FullDefenceResponse) {
            content.putAll(
                fullDefenceResponseContentProvider.createContent((FullDefenceResponse) defendantResponse)
            );
        } else if (defendantResponse instanceof FullAdmissionResponse) {
            content.putAll(
                fullAdmissionResponseContentProvider.createContent((FullAdmissionResponse) defendantResponse)
            );
        }
        return content;
    }
}
