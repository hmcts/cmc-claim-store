package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.Interest.InterestType;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatPercent;
import static uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator.calculateInterest;

public class AmountContentProvider {

    private final InterestCalculationService interestCalculationService;

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
        LocalDate interestToDate;
        if (!interest.getType().equals(InterestType.NO_INTEREST)) {
            dailyAmount = interestCalculationService.calculateDailyAmountFor(
                ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount(),
                interest.getRate()
            );

            if (claim.getClaimData().getInterestDate().getEndDate()
                .equals(InterestDate.InterestEndDateType.SUBMISSION)) {
                interestToDate = claim.getIssuedOn();
            } else {
                interestToDate = claim.getCountyCourtJudgmentRequestedAt().toLocalDate();
            }

            interestFromDate = Optional.of(getInterestFromDate(claim));
            interestRate = interest.getRate();
            interestAmount = getInterestAmount(
                claim,
                interestToDate,
                claimAmount
            );
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
        Claim claim,
        LocalDate toDate,
        BigDecimal claimAmount
    ) {
        if (!claim.getClaimData().getInterest().getType()
            .equals(InterestType.NO_INTEREST)) {
            return calculateInterest(
                claimAmount,
                claim.getClaimData().getInterest().getRate(),
                getInterestFromDate(claim),
                toDate
            );
        }
        return BigDecimal.ZERO;
    }

    private static LocalDate getInterestFromDate(Claim claim) {
        if (claim.getClaimData().getInterestDate().getType().equals(InterestDate.InterestDateType.CUSTOM)) {
            InterestDate interestDate = claim.getClaimData().getInterestDate();
            return interestDate.getDate();
        } else {
            return claim.getIssuedOn();
        }
    }
}
