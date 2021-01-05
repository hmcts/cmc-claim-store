package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Calculates directions questionnaire submission deadline.
 */
@Component
public class HWFCaseWorkerRespondSlaCalculator {

    private final WorkingDayIndicator workingDayIndicator;
    private final long serviceDays;
    private final long timeForResponseInDays;
    private final int endOfBusinessDayHour;

    public HWFCaseWorkerRespondSlaCalculator(
        WorkingDayIndicator workingDayIndicator,
        @Value("${hwfCaseWorkerRespondSla.serviceDays}") long serviceDays,
        @Value("${hwfCaseWorkerRespondSla.responseDays}") long timeForResponseInDays,
        @Value("${hwfCaseWorkerRespondSla.endOfBusinessDayHour}") int endOfBusinessDayHour
    ) {
        this.workingDayIndicator = workingDayIndicator;
        this.serviceDays = serviceDays;
        this.timeForResponseInDays = timeForResponseInDays;
        this.endOfBusinessDayHour = endOfBusinessDayHour;
    }

    public LocalDate calculate(LocalDateTime claimCreationAt) {
        LocalDate date = claimCreationAt.toLocalDate().plusDays(timeForResponseInDays);

        if (isTooLateForToday(claimCreationAt.getHour())) {
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
