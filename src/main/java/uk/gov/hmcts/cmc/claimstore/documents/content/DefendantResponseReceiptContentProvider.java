package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ResponseData;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class DefendantResponseReceiptContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimContentProvider claimContentProvider;

    public DefendantResponseReceiptContentProvider(
        final PartyDetailsContentProvider partyDetailsContentProvider,
        final ClaimContentProvider claimContentProvider
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
        this.claimContentProvider = claimContentProvider;
    }

    public Map<String, Object> createContent(final Claim claim) {
        requireNonNull(claim);
        ResponseData defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);

        Map<String, Object> content = new HashMap<>();
        content.put("claimReferenceNumber", claim.getReferenceNumber());
        content.put("claimSubmittedOn", formatDate(claim.getCreatedAt()));
        content.put("claim", claimContentProvider.createContent(claim));
        content.put("response", claim.getRespondedAt()); //?

        content.put("freeMediation", defendantResponse.getFreeMediation()); //?

        content.put("defenceSubmittedOn", formatDate(claim.getRespondedAt()));
        content.put("defendant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            defendantResponse.getDefendant(),
            claim.getDefendantEmail()
        ));

        content.put("claimant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            claim.getClaimData().getClaimant(),
            claim.getSubmitterEmail()
        ));

        content.put("responseDefence", defendantResponse.getDefence());

        Optional<StatementOfTruth> optionalStatementOfTruth = defendantResponse.getStatementOfTruth();
        content.put("signerName", optionalStatementOfTruth.map((StatementOfTruth::getSignerName)).orElse(null));
        content.put("signerRole", optionalStatementOfTruth.map((StatementOfTruth::getSignerRole)).orElse(null));content.put("signerRole", optionalStatementOfTruth.map((StatementOfTruth::getSignerRole)).orElse(null));

        content.put("freeMediation", defendantResponse.getFreeMediation().orElse(ResponseData.FreeMediationOption.NO).name().toLowerCase());

        return content;
    }
}
