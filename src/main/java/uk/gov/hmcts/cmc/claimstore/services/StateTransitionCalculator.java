package uk.gov.hmcts.cmc.claimstore.services;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StateTransitionCalculator {
    private final int numberOfDays;
    private final WorkingDayIndicator workingDayIndicator;

    public StateTransitionCalculator(
        WorkingDayIndicator workingDayIndicator,
        int numberOfDays
    ) {
        this.numberOfDays = numberOfDays;
        this.workingDayIndicator = workingDayIndicator;
    }

    public LocalDate calculateDeadlineFromDate(LocalDate respondedAt) {
        LocalDate intentionToProceedDeadline = respondedAt.plusDays(this.numberOfDays);
        return workingDayIndicator.getNextWorkingDay(intentionToProceedDeadline);
    }

    public LocalDate calculateDateFromDeadline(LocalDateTime runDateTime) {
        //4pm cut off for court working days
        int adjustDays = runDateTime.getHour() >= 16 ? 0 : 1;
        LocalDate adjustedDate = runDateTime.toLocalDate().minusDays(adjustDays);

        return workingDayIndicator.getPreviousWorkingDay(adjustedDate).minusDays(this.numberOfDays);
    }

}
