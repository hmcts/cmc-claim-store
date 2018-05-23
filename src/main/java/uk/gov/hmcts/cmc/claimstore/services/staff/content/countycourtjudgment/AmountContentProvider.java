package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.NO_INTEREST;

@Component
public class AmountContentProvider {

    private final InterestContentProvider interestContentProvider;

    public AmountContentProvider(InterestContentProvider interestContentProvider) {
        this.interestContentProvider = interestContentProvider;
    }

    public AmountContent create(Claim claim) {
        BigDecimal claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
        BigDecimal paidAmount = claim.getCountyCourtJudgment().getPaidAmount().orElse(ZERO);
        InterestContent interestContent = null;
        BigDecimal interestRealValue = ZERO;

        if (claim.getClaimData().getInterest().getType() != NO_INTEREST) {
            interestContent = interestContentProvider.createContent(
                claim.getClaimData().getInterest(),
                claim.getClaimData().getInterest().getInterestDate(),
                claimAmount,
                claim.getIssuedOn(),
                LocalDateTimeFactory.nowInLocalZone().toLocalDate()
            );
            interestRealValue = interestContent.getAmountRealValue();
        }

        return new AmountContent(
            formatMoney(claimAmount),
            formatMoney(claimAmount
                .add(claim.getClaimData().getFeesPaidInPound())
                .add(interestRealValue)),
            interestContent,
            formatMoney(claim.getClaimData().getFeesPaidInPound()),
            formatMoney(paidAmount),
            formatMoney(claimAmount
                .add(claim.getClaimData().getFeesPaidInPound())
                .add(interestRealValue)
                .subtract(paidAmount))
        );

    }

}
