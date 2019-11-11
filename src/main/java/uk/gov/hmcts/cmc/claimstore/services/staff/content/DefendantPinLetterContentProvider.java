package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.claimstore.utils.PartyNameUtils.getPartyNameFor;
import static uk.gov.hmcts.cmc.claimstore.utils.Preconditions.requireNonBlank;

@Component
public class DefendantPinLetterContentProvider {

    private final NotificationsProperties notificationsProperties;
    private final StaffEmailProperties staffEmailProperties;
    private final InterestContentProvider interestContentProvider;

    public DefendantPinLetterContentProvider(
        NotificationsProperties notificationsProperties,
        StaffEmailProperties staffEmailProperties,
        InterestContentProvider interestContentProvider
    ) {
        this.notificationsProperties = notificationsProperties;
        this.staffEmailProperties = staffEmailProperties;
        this.interestContentProvider = interestContentProvider;
    }

    public Map<String, Object> createContent(Claim claim, String defendantPin) {
        requireNonNull(claim);
        requireNonBlank(defendantPin);

        List<BigDecimal> totalAmountComponents = new ArrayList<>();
        totalAmountComponents.add(((AmountBreakDown) claim.getClaimData()
            .getAmount())
            .getTotalAmount());
        totalAmountComponents.add(claim.getClaimData()
            .getFeesPaidInPounds().orElse(ZERO));

        if (!claim.getClaimData()
            .getInterest()
            .getType()
            .equals(Interest.InterestType.NO_INTEREST)) {
            InterestContent interestContent = interestContentProvider.createContent(
                claim.getClaimData()
                    .getInterest(),
                claim.getClaimData()
                    .getInterest()
                    .getInterestDate(),
                ((AmountBreakDown) claim.getClaimData()
                    .getAmount())
                    .getTotalAmount(),
                claim.getIssuedOn(),
                claim.getIssuedOn()
            );
            totalAmountComponents.add(interestContent.getAmountRealValue());
        }

        Map<String, Object> content = new HashMap<>();
        content.put("claimantFullName", getPartyNameFor(claim.getClaimData()
            .getClaimant())
        );
        content.put("defendantFullName", claim.getClaimData()
            .getDefendant()
            .getName()
        );
        content.put("claimTotalAmount", formatMoney(
            totalAmountComponents.stream()
                .filter(Objects::nonNull)
                .reduce(ZERO, BigDecimal::add))
        );
        content.put("respondToClaimUrl", notificationsProperties.getRespondToClaimUrl());
        content.put("claimReferenceNumber", claim.getReferenceNumber());
        content.put("defendantPin", defendantPin);
        content.put("responseDeadline", formatDate(claim.getResponseDeadline()));
        content.put("defendantAddress", claim.getClaimData()
            .getDefendant()
            .getAddress()
        );
        content.put("hmctsEmail", staffEmailProperties.getRecipient());

        return content;
    }
}
