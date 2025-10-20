package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Calculates issue date from submission date time.
 */
@Component
public class IssueDateCalculator {

    /**
     * Time (hour) at submitted claim will not be issued the same business day (but the next business day).
     */
    private final int endOfBusinessDayHour;

    private final WorkingDayIndicator workingDayIndicator;

    public IssueDateCalculator(WorkingDayIndicator workingDayIndicator,
                               @Value("${dateCalculations.endOfBusinessDayHour}") int endOfBusinessDayHour) {
        this.workingDayIndicator = workingDayIndicator;
        this.endOfBusinessDayHour = endOfBusinessDayHour;
    }

    public LocalDate calculateIssueDay(LocalDateTime dateTime) {

        LocalDate date = dateTime.toLocalDate();

        if (isTooLateForToday(dateTime)) {
            date = date.plusDays(1);
        }

        while (!workingDayIndicator.isWorkingDay(date)) {
            date = date.plusDays(1);
        }

        return date;
    }

    private boolean isTooLateForToday(LocalDateTime dateTime) {
        return dateTime.getHour() >= endOfBusinessDayHour;
    }
}

