package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestBreakdownContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatPercent;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.BREAKDOWN;

@Component
public class InterestContentProvider {

    private final InterestCalculationService interestCalculationService;

    @Autowired
    public InterestContentProvider(InterestCalculationService interestCalculationService) {
        this.interestCalculationService = interestCalculationService;
    }

    public InterestContent createContent(
        Interest interest,
        InterestDate interestDate,
        BigDecimal claimAmount,
        LocalDate issuedOn,
        LocalDate interestEndDate
    ) {
        requireNonNull(interest);
        requireNonNull(interestDate);
        requireNonNull(claimAmount);
        requireNonNull(issuedOn);

        if (interest.getType() == BREAKDOWN) {
            return createBreakdownInterestContent(
                interest,
                interestDate,
                claimAmount,
                issuedOn,
                interestEndDate
            );
        }

        return createSameRateForWholePeriodInterestContent(
            interest,
            interestDate,
            claimAmount,
            issuedOn,
            interestEndDate
        );
    }

    private InterestContent createSameRateForWholePeriodInterestContent(
        Interest interest,
        InterestDate interestDate,
        BigDecimal claimAmount,
        LocalDate issuedOn,
        LocalDate interestEndDate
    ) {
        LocalDate fromDate = interestDate.isCustom() ? interestDate.getDate() : issuedOn;
        LocalDate endDate = interestDate.isEndDateOnSubmission() ? issuedOn : interestEndDate;
        BigDecimal dailyAmount = interestCalculationService.calculateDailyAmountFor(claimAmount, interest.getRate());
        BigDecimal amountUpToNowRealValue = BigDecimal.ZERO;

        if (!fromDate.isAfter(LocalDateTimeFactory.nowInLocalZone().toLocalDate())) {
            amountUpToNowRealValue = interestCalculationService.calculateInterestUpToDate(
                claimAmount, interest.getRate(), fromDate, endDate
            );
        }

        return new InterestContent(
            interest.getType().name(),
            formatPercent(interest.getRate()),
            interest.getType().equals(Interest.InterestType.DIFFERENT),
            interest.getReason(),
            interestDate.isCustom(),
            formatDate(fromDate),
            formatMoney(amountUpToNowRealValue),
            amountUpToNowRealValue,
            formatMoney(dailyAmount),
            interestDate.getReason(),
            interestDate.isEndDateOnSubmission()
        );
    }

    private InterestContent createBreakdownInterestContent(
        Interest interest,
        InterestDate interestDate,
        BigDecimal claimAmount,
        LocalDate issuedOn,
        LocalDate endDate
    ) {
        Optional<BigDecimal> dailyAmount = inferDailyInterestAmount(interest, interestDate, claimAmount);

        BigDecimal amountUpToNowRealValue = TotalAmountCalculator.calculateBreakdownInterest(
            interest,
            interestDate,
            claimAmount,
            issuedOn,
            endDate
        );

        return new InterestContent(
            interest.getType().name(),
            new InterestBreakdownContent(
                formatMoney(amountUpToNowRealValue),
                interest.getInterestBreakdown().getExplanation()
            ),
            dailyAmount.map(Formatting::formatMoney).orElse(null),
            interestDate.getEndDateType().name(),
            amountUpToNowRealValue
        );
    }

    private Optional<BigDecimal> inferDailyInterestAmount(
        Interest interest,
        InterestDate interestDate,
        BigDecimal claimAmount
    ) {
        if (interestDate.isEndDateOnSubmission()) {
            return Optional.empty();
        }

        BigDecimal dailyAmount = interest.getSpecificDailyAmount().orElseGet(
            () -> interestCalculationService.calculateDailyAmountFor(claimAmount, interest.getRate())
        );

        return Optional.of(dailyAmount);
    }

}
