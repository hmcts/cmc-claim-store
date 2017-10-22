package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.PublicHolidaysCollection;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Tells if given day is a working day.
 */
@Component
public class WorkingDayIndicator {

    private PublicHolidaysCollection publicHolidaysCollection;

    public WorkingDayIndicator(PublicHolidaysCollection publicHolidaysApiClient) {
        this.publicHolidaysCollection = publicHolidaysApiClient;
    }

    /**
     * Verifies if given date is a working day in UK (England and Wales only).
     */
    public boolean isWorkingDay(LocalDate date) {
        return !isWeekend(date) && !isPublicHoliday(date);
    }

    public boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public boolean isPublicHoliday(LocalDate date) {
        return publicHolidaysCollection.getPublicHolidays().contains(date);
    }
}
