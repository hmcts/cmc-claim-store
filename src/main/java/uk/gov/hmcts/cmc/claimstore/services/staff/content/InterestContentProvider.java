package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatPercent;

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
        LocalDate issuedOn) {
        requireNonNull(interest);
        requireNonNull(interestDate);
        requireNonNull(claimAmount);
        requireNonNull(issuedOn);

        boolean customInterestDate = interestDate.getType().equals(InterestDate.InterestDateType.CUSTOM);
        LocalDate fromDate;
        String interestDateReason = null;
        BigDecimal amountUpToNowRealValue;
        String amountUpToNow;
        if (customInterestDate) {
            fromDate = interestDate.getDate();
            interestDateReason = interestDate.getReason();
        } else {
            fromDate = issuedOn;
        }

        if (fromDate.isAfter(LocalDateTimeFactory.nowInLocalZone().toLocalDate())) {
            fromDate = LocalDateTimeFactory.nowInLocalZone().toLocalDate();
        }

        amountUpToNowRealValue = interestCalculationService.calculateInterestUpToNow(
            claimAmount, interest.getRate(), fromDate
        );
        amountUpToNow = formatMoney(amountUpToNowRealValue);
        BigDecimal dailyAmount = interestCalculationService.calculateDailyAmountFor(claimAmount, interest.getRate());

        return new InterestContent(
            formatPercent(interest.getRate()),
            interest.getType().equals(Interest.InterestType.DIFFERENT),
            interest.getReason(),
            customInterestDate,
            formatDate(fromDate),
            amountUpToNow,
            amountUpToNowRealValue,
            formatMoney(dailyAmount),
            interestDateReason,
            interestDate.getEndDate().equals(InterestDate.InterestEndDateType.SUBMISSION)
        );
    }

}
