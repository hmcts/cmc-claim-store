package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.Interest;
import uk.gov.hmcts.cmc.claimstore.models.InterestDate;
import uk.gov.hmcts.cmc.claimstore.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public class AmountContentProvider {

    private InterestCalculationService interestCalculationService;

    public AmountContentProvider(InterestCalculationService interestCalculationService) {
        this.interestCalculationService = interestCalculationService;
    }

    public AmountContent create(Claim claim) {
        BigDecimal claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
        BigDecimal paidAmount = claim.getCountyCourtJudgment().getPaidAmount().orElse(BigDecimal.ZERO);

        BigDecimal dailyAmount = BigDecimal.ZERO;
        BigDecimal interestAmount = BigDecimal.ZERO;
        if (!claim.getClaimData().getInterest().getType().equals(Interest.InterestType.NO_INTEREST)) {
            dailyAmount = interestCalculationService.calculateDailyAmountFor(
                ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount(),
                claim.getClaimData().getInterest().getRate()
            );

            interestAmount = getInterestAmount(
                interestCalculationService, claim, claim.getCountyCourtJudgmentRequestedAt()
                    .toLocalDate(), claimAmount)
            ;
        }

        return new AmountContent(
            formatMoney(claimAmount),
            formatMoney(interestAmount),
            formatMoney(dailyAmount),
            formatMoney(claim.getClaimData().getFeesPaidInPound()),
            formatMoney(paidAmount),
            formatMoney(claimAmount
                .add(claim.getClaimData().getFeesPaidInPound())
                .add(interestAmount)
                .subtract(paidAmount))
        );

    }

    private static BigDecimal getInterestAmount(
        final InterestCalculationService interestCalculationService,
        final Claim claim,
        final LocalDate toDate,
        final BigDecimal claimAmount
    ) {
        if (!claim.getClaimData().getInterest().getType()
            .equals(uk.gov.hmcts.cmc.claimstore.models.Interest.InterestType.NO_INTEREST)) {
            return interestCalculationService.calculateInterest(
                claimAmount,
                claim.getClaimData().getInterest().getRate(),
                getInterestFromDate(claim),
                toDate
            );
        }
        return BigDecimal.ZERO;
    }

    private static LocalDate getInterestFromDate(final Claim claim) {
        if (claim.getClaimData().getInterestDate().getType().equals(InterestDate.InterestDateType.CUSTOM)) {
            InterestDate interestDate = claim.getClaimData().getInterestDate();
            return interestDate.getDate();
        } else {
            return claim.getCreatedAt().toLocalDate();
        }
    }
}
