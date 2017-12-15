package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.InterestAmount;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class InterestRateService {

    public InterestAmount calculateRate(LocalDate fromDate, LocalDate toDate, double rate, double amount) {
        validateNumberIsNonNegative(rate);
        validateNumberIsNonNegative(amount);

        final double noOfDays = calculateNumberOfDays(fromDate, toDate);

        return InterestAmount.valueOf((amount * noOfDays * rate) / (365 * 100));
    }

    private double calculateNumberOfDays(final LocalDate fromDate, final LocalDate toDate) {
        validatePeriod(fromDate, toDate);
        return fromDate.until(toDate, ChronoUnit.DAYS);
    }

    private void validatePeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDateDate or toDateDate is null");
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalStateException("fromDateDate must not be after toDateDate");
        }
    }

    private void validateNumberIsNonNegative(final double value) {
        if (value < 0) {
            throw new IllegalArgumentException("value must be >= 0");
        }
    }

}
