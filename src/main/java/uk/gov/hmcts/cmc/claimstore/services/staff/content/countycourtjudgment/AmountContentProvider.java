package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmccase.models.Interest;
import uk.gov.hmcts.cmccase.models.Interest.InterestType;
import uk.gov.hmcts.cmccase.models.InterestDate;
import uk.gov.hmcts.cmccase.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatPercent;

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
        BigDecimal interestRate = BigDecimal.ZERO;
        Interest interest = claim.getClaimData().getInterest();
        Optional<LocalDate> interestFromDate = Optional.empty();
        if (!interest.getType().equals(InterestType.NO_INTEREST)) {
            dailyAmount = interestCalculationService.calculateDailyAmountFor(
                ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount(),
                interest.getRate()
            );

            InterestDate interestDate = claim.getClaimData().getInterestDate();
            if (interestDate.getType().equals(InterestDate.InterestDateType.CUSTOM)) {
                interestFromDate = Optional.of(interestDate.getDate());
            } else {
                interestFromDate = Optional.of(claim.getCreatedAt().toLocalDate());
            }

            interestRate = interest.getRate();
            interestAmount = getInterestAmount(
                interestCalculationService, claim, claim.getCountyCourtJudgmentRequestedAt()
                    .toLocalDate(), claimAmount)
            ;
        }
        InterestContent interestContent = new InterestContent(
            formatPercent(interestRate),
            interestFromDate.map(Formatting::formatDate)
                .orElse("No interest claimed"),
            formatMoney(interestAmount),
            formatMoney(dailyAmount)
        );

        return new AmountContent(
            formatMoney(claimAmount),
            interestContent,
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
            .equals(InterestType.NO_INTEREST)) {
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
