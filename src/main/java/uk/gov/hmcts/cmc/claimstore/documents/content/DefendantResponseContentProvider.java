package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_RESPONSE;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class DefendantResponseContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimDataContentProvider claimDataContentProvider;
    private final NotificationsProperties notificationsProperties;
    private final FullDefenceResponseContentProvider fullDefenceResponseContentProvider;
    private final FullAdmissionResponseContentProvider fullAdmissionResponseContentProvider;
    private final PartAdmissionResponseContentProvider partAdmissionResponseContentProvider;

    public DefendantResponseContentProvider(
        PartyDetailsContentProvider partyDetailsContentProvider,
        ClaimDataContentProvider claimDataContentProvider,
        NotificationsProperties notificationsProperties,
        FullDefenceResponseContentProvider fullDefenceResponseContentProvider,
        FullAdmissionResponseContentProvider fullAdmissionResponseContentProvider,
        PartAdmissionResponseContentProvider partAdmissionResponseContentProvider
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
        this.claimDataContentProvider = claimDataContentProvider;
        this.notificationsProperties = notificationsProperties;
        this.fullDefenceResponseContentProvider = fullDefenceResponseContentProvider;
        this.fullAdmissionResponseContentProvider = fullAdmissionResponseContentProvider;
        this.partAdmissionResponseContentProvider = partAdmissionResponseContentProvider;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);
        Response defendantResponse = claim.getResponse()
            .orElseThrow(() -> new IllegalStateException(MISSING_RESPONSE));

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

        switch (defendantResponse.getResponseType()) {
            case FULL_DEFENCE:
                claim.getTotalAmountTillToday().ifPresent(
                    amount -> content.put("amount", formatMoney(amount))
                );
                content.putAll(
                    fullDefenceResponseContentProvider.createContent((FullDefenceResponse) defendantResponse)
                );
                break;
            case FULL_ADMISSION:
                content.putAll(fullAdmissionResponseContentProvider
                    .createContent((FullAdmissionResponse) defendantResponse,
                        claim.getTotalAmountTillToday()
                            .orElseThrow(() -> new IllegalArgumentException("Claim amount cant be empty or null"))));
                break;
            case PART_ADMISSION:
                content.putAll(
                    partAdmissionResponseContentProvider.createContent((PartAdmissionResponse) defendantResponse)
                );
                break;
            default:
                throw new MappingException("Invalid responseType " + defendantResponse.getResponseType());
        }
        return content;
    }
}
