package uk.gov.hmcts.cmc.claimstore.services;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Calculates directions questionnaire submission deadline.
 */
@Component
@DisallowConcurrentExecution
public class HWFCaseWorkerRespondSlaCalculator {

    private final WorkingDayIndicator workingDayIndicator;

    private final int timeForResponseInDays;

    private final int endOfBusinessDayHour;

    public HWFCaseWorkerRespondSlaCalculator(
        WorkingDayIndicator workingDayIndicator,
        @Value("${hwfCaseWorkerRespondSla.timeForResponseInDays}") int timeForResponseInDays,
        @Value("${hwfCaseWorkerRespondSla.endOfBusinessDayHour}") int endOfBusinessDayHour
    ) {
        this.workingDayIndicator = workingDayIndicator;
        this.timeForResponseInDays = timeForResponseInDays;
        this.endOfBusinessDayHour = endOfBusinessDayHour;
    }

    public LocalDate calculate(LocalDateTime claimCreationAt) {
        LocalDateTime newDateTime = null;
        LocalDate date = null;

        newDateTime = claimCreationAt.plusDays(timeForResponseInDays).plusDays(2);
        date = claimCreationAt.toLocalDate().plusDays(timeForResponseInDays).plusDays(2);

        if (isTooLateForToday(newDateTime.getHour())) {
            date = date.plusDays(1);
        }

        while (!workingDayIndicator.isWorkingDay(date)) {
            date = date.plusDays(1);
        }

        return date;

    }

    private boolean isTooLateForToday(int hour) {
        return hour >= endOfBusinessDayHour;
    }
}
