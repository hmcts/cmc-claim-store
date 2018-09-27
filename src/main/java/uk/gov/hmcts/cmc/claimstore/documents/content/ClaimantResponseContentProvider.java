package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;

@Component
public class ClaimantResponseContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimDataContentProvider claimDataContentProvider;
    private final NotificationsProperties notificationsProperties;

    public ClaimantResponseContentProvider(
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
        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);
        Response defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);

        Map<String, Object> content = new HashMap<>();

        content.put("claim", claimDataContentProvider.createContent(claim));
        claim.getClaimantRespondedAt().ifPresent(respondedAt ->
        {
            content.put("claimantSubmittedOn", formatDateTime(respondedAt));
            content.put("claimantSubmittedDate", formatDate(respondedAt));
        });

        content.put("amountPaid", claimantResponse.getAmountPaid());
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

        if (claimantResponse instanceof ResponseAcceptation) {
            content.put("responseType", "acceptation");


        } else if (claimantResponse instanceof ResponseRejection) {
            content.put("responseType", "rejection");

        }
        return content;
    }
}
