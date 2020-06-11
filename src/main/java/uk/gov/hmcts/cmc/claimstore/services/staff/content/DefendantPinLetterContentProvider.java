package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.ccd.util.PartyNameUtils.getPartyNameFor;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
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

        BigDecimal totalAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
        BigDecimal fees = claim.getClaimData().getFeesPaidInPounds().orElse(ZERO);
        BigDecimal interest = ZERO;

        if (!Interest.InterestType.NO_INTEREST.equals(claim.getClaimData().getInterest().getType())) {
            LocalDate issuedOn = claim.getIssuedOn()
                .orElseThrow(() -> new IllegalStateException("Missing issuedOn date"));
            InterestContent interestContent = interestContentProvider.createContent(
                claim.getClaimData().getInterest(),
                claim.getClaimData().getInterest().getInterestDate(),
                ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount(),
                issuedOn,
                issuedOn
            );
            interest = interestContent.getAmountRealValue();
        }

        return Map.of(
            "claimantFullName", getPartyNameFor(claim.getClaimData().getClaimant()),
            "defendantFullName", claim.getClaimData().getDefendant().getName(),
            "claimTotalAmount", formatMoney(totalAmount.add(fees).add(interest)),
            "respondToClaimUrl", notificationsProperties.getRespondToClaimUrl(),
            "claimReferenceNumber", claim.getReferenceNumber(),
            "defendantPin", defendantPin,
            "responseDeadline", formatDate(claim.getResponseDeadline()),
            "defendantAddress", claim.getClaimData().getDefendant().getAddress(),
            "hmctsEmail", staffEmailProperties.getRecipient()
        );
    }
}
