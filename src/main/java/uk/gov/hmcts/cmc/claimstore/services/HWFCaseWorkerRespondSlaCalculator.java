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

    @Value("${hwfCaseWorkerRespondSla.serviceDays}")
    private final int serviceDays;

    @Value("${hwfCaseWorkerRespondSla.responseDays}")
    private final int timeForResponseInDays;

    @Value("${hwfCaseWorkerRespondSla.endOfBusinessDayHour}")
    private final int endOfBusinessDayHour;

    public HWFCaseWorkerRespondSlaCalculator(
        WorkingDayIndicator workingDayIndicator,
        int serviceDays,
        int timeForResponseInDays,
        int endOfBusinessDayHour
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
