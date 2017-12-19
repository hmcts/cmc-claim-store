package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class InterestRateService {

    public BigDecimal calculateRate(LocalDate fromDate, LocalDate toDate, BigDecimal rate, BigDecimal amount) {
        validateVarIsNonNegativeBigDecimal(rate);
        validateVarIsNonNegativeBigDecimal(amount);

        long noOfDays = calculateNumberOfDays(fromDate, toDate);

        return amount
            .multiply(rate)
            .multiply(BigDecimal.valueOf(noOfDays))
            .divide(BigDecimal.valueOf(365 * 100), 2, RoundingMode.HALF_UP);
    }

    private long calculateNumberOfDays(LocalDate fromDate, LocalDate toDate) {
        validatePeriod(fromDate, toDate);
        return fromDate.until(toDate, ChronoUnit.DAYS);
    }

    private void validatePeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new BadRequestException("fromDate or toDate is null");
        }

        if (fromDate.isAfter(toDate)) {
            throw new BadRequestException("fromDate must not be after toDate");
        }
    }

    private void validateVarIsNonNegativeBigDecimal(BigDecimal value) {
        if (value == null || value.signum() == -1) {
            throw new BadRequestException("value must be >= 0");
        }
    }
}
