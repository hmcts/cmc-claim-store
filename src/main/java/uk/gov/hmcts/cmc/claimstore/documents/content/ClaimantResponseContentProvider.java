package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class ClaimantResponseContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimDataContentProvider claimDataContentProvider;
    private final NotificationsProperties notificationsProperties;
    private final ResponseAcceptationContentProvider responseAcceptationContentProvider;
    private final ResponseRejectionContentProvider responseRejectionContentProvider;

    public ClaimantResponseContentProvider(
        PartyDetailsContentProvider partyDetailsContentProvider,
        ClaimDataContentProvider claimDataContentProvider,
        NotificationsProperties notificationsProperties,
        ResponseAcceptationContentProvider responseAcceptationContentProvider,
        ResponseRejectionContentProvider responseRejectionContentProvider
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
        this.claimDataContentProvider = claimDataContentProvider;
        this.notificationsProperties = notificationsProperties;
        this.responseAcceptationContentProvider = responseAcceptationContentProvider;
        this.responseRejectionContentProvider = responseRejectionContentProvider;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);

        Map<String, Object> content = new HashMap<>();

        content.put("claim", claimDataContentProvider.createContent(claim));
        claim.getClaimantRespondedAt().ifPresent(respondedAt -> {
            content.put("claimantSubmittedOn", formatDateTime(respondedAt));
            content.put("claimantSubmittedDate", formatDate(respondedAt));
        });

        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);
        content.put("amountPaid", claimantResponse.getAmountPaid());
        content.put("responseDashboardUrl", notificationsProperties.getFrontendBaseUrl());

        Response defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);
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

        content.put("responseType", claimantResponse.getType().name());

        switch (claimantResponse.getType()) {
            case ACCEPTATION: {
                content.put("defendantAdmissionAccepted", "I accept this amount");
                ResponseAcceptation responseAcceptation = (ResponseAcceptation) claimantResponse;
                content.putAll(responseAcceptationContentProvider.createContent(responseAcceptation));

                claim.getTotalAmountTillDateOfIssue().ifPresent(
                    totalAmount -> content.put("totalAmount", formatMoney(totalAmount.subtract(claimantResponse.getAmountPaid().orElse(BigDecimal.ZERO))))
                );

                if (responseAcceptation.getFormaliseOption().isPresent()) {
                    FormaliseOption formaliseOption = responseAcceptation.getFormaliseOption().get();
                    content.put("formaliseOption", formaliseOption.getDescription());
                    addFormalisedOption(claim, content, formaliseOption);
                }
            }
            break;
            case REJECTION:
                content.put("defendantAdmissionAccepted", "I reject this amount");
                content.putAll(responseRejectionContentProvider.createContent((ResponseRejection) claimantResponse));
                break;
            default:
                throw new MappingException("Invalid responseType " + claimantResponse.getType());

        }

        return content;
    }

    private void addFormalisedOption(
        Claim claim,
        Map<String, Object> content,
        FormaliseOption formaliseOption
    ) {
        switch (formaliseOption) {
            case CCJ:
                content.put("ccj", claim.getCountyCourtJudgment());
                break;
            case SETTLEMENT:
            case REFER_TO_JUDGE:
                //No Action
                break;
            default:
                throw new MappingException("Invalid formalization type " + formaliseOption);
        }
    }
}
