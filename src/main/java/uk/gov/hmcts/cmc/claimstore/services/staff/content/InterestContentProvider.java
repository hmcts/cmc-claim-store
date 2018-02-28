package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        LocalDateTime submittedOn) {
        requireNonNull(interest);
        requireNonNull(interestDate);
        requireNonNull(claimAmount);
        requireNonNull(submittedOn);

        boolean customInterestDate = interestDate.getType().equals(InterestDate.InterestDateType.CUSTOM);
        String fromDate;
        BigDecimal amountUpToNowRealValue = null;
        String amountUpToNow = null;
        if (customInterestDate) {
            fromDate = formatDate(interestDate.getDate());
            amountUpToNowRealValue = interestCalculationService.calculateInterestUpToNow(
                claimAmount, interest.getRate(), interestDate.getDate()
            );
            amountUpToNow = formatMoney(amountUpToNowRealValue);
        } else {
            fromDate = formatDate(submittedOn);
        }
        BigDecimal dailyAmount = interestCalculationService.calculateDailyAmountFor(claimAmount, interest.getRate());

        return new InterestContent(
            formatPercent(interest.getRate()),
            interest.getType().equals(Interest.InterestType.DIFFERENT),
            interest.getReason(),
            customInterestDate,
            fromDate,
            amountUpToNow,
            amountUpToNowRealValue,
            formatMoney(dailyAmount)
        );
    }

}
