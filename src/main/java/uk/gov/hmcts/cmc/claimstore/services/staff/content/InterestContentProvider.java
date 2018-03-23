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
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType.SUBMISSION;

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
        LocalDate issuedOn
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
                issuedOn
            );
        } else {
            return createSameRateForWholePeriodInterestContent(
                interest,
                interestDate,
                claimAmount,
                issuedOn
            );
        }
    }

    private InterestContent createSameRateForWholePeriodInterestContent(
        Interest interest,
        InterestDate interestDate,
        BigDecimal claimAmount,
        LocalDate issuedOn
    ) {
        boolean customInterestDate = interestDate.getType().equals(InterestDate.InterestDateType.CUSTOM);
        LocalDate fromDate;
        String interestDateReason = null;
        BigDecimal amountUpToNowRealValue = null;
        String amountUpToNow = null;
        if (customInterestDate) {
            fromDate = interestDate.getDate();
            interestDateReason = interestDate.getReason();
        } else {
            fromDate = issuedOn;
        }

        if (!fromDate.isAfter(LocalDateTimeFactory.nowInLocalZone().toLocalDate())) {
            amountUpToNowRealValue = interestCalculationService.calculateInterestUpToNow(
                claimAmount, interest.getRate(), fromDate
            );
            amountUpToNow = formatMoney(amountUpToNowRealValue);
        }

        BigDecimal dailyAmount = interestCalculationService.calculateDailyAmountFor(claimAmount, interest.getRate());

        return new InterestContent(
            interest.getType().name(),
            formatPercent(interest.getRate()),
            interest.getType().equals(Interest.InterestType.DIFFERENT),
            interest.getReason(),
            customInterestDate,
            formatDate(fromDate),
            amountUpToNow,
            amountUpToNowRealValue,
            formatMoney(dailyAmount),
            interestDateReason,
            interestDate.getEndDateType().equals(SUBMISSION)
        );
    }

    private InterestContent createBreakdownInterestContent(
        Interest interest,
        InterestDate interestDate,
        BigDecimal claimAmount,
        LocalDate issuedOn
    ) {
        Optional<BigDecimal> dailyAmount = inferDailyInterestAmount(interest, interestDate, claimAmount);
        BigDecimal amountUpToNowRealValue = TotalAmountCalculator.calculateBreakdownInterest(
            interest, interestDate, claimAmount, issuedOn, LocalDateTimeFactory.nowInLocalZone().toLocalDate()
        );

        return new InterestContent(
            interest.getType().name(),
            new InterestBreakdownContent(
                formatMoney(interest.getInterestBreakdown().getTotalAmount()),
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
        if (interestDate.getEndDateType() == SUBMISSION) {
            return Optional.empty();
        }

        BigDecimal dailyAmount;
        if (interest.getSpecificDailyAmount().isPresent()) {
            dailyAmount = interest.getSpecificDailyAmount().get();
        } else {
            dailyAmount = interestCalculationService.calculateDailyAmountFor(claimAmount, interest.getRate());
        }
        return Optional.of(dailyAmount);
    }

}
