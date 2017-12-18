package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class InterestRateService {

    public BigDecimal calculateRate(LocalDate fromDate, LocalDate toDate, BigDecimal rate, BigDecimal amount) {
        validateVarIsNonNegativeBigDecimal(rate);
        validateVarIsNonNegativeBigDecimal(amount);

        long noOfDays = calculateNumberOfDays(fromDate, toDate);

        return BigDecimal.valueOf((amount.doubleValue() * noOfDays * rate.doubleValue()) / (365 * 100));
    }

    private long calculateNumberOfDays(LocalDate fromDate, LocalDate toDate) {
        validatePeriod(fromDate, toDate);
        return fromDate.until(toDate, ChronoUnit.DAYS);
    }

    private void validatePeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new BadRequestException("fromDateDate or toDateDate is null");
        }

        if (fromDate.isAfter(toDate)) {
            throw new BadRequestException("fromDateDate must not be after toDateDate");
        }
    }

    private void validateVarIsNonNegativeBigDecimal(BigDecimal value) {
        if (value == null || value.signum() == -1) {
            throw new BadRequestException("value must be >= 0");
        }
    }
}
