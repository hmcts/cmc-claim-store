package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Calculates directions questionnaire submission deadline.
 */
@Component
public class DirectionsQuestionnaireDeadlineCalculator {

    private final WorkingDayIndicator workingDayIndicator;
    private final long serviceDays;
    private final long timeForResponseInDays;
    private final int endOfBusinessDayHour;

    public DirectionsQuestionnaireDeadlineCalculator(
        WorkingDayIndicator workingDayIndicator,
        @Value("${dateCalculations.serviceDays}") long serviceDays,
        @Value("${dateCalculations.responseDays}") long timeForResponseInDays,
        @Value("${dateCalculations.endOfBusinessDayHour}") int endOfBusinessDayHour
    ) {
        this.workingDayIndicator = workingDayIndicator;
        this.serviceDays = serviceDays;
        this.timeForResponseInDays = timeForResponseInDays;
        this.endOfBusinessDayHour = endOfBusinessDayHour;
    }

    public LocalDate calculateDirectionsQuestionnaireDeadline(LocalDateTime respondedAt) {
        LocalDate date = respondedAt.toLocalDate().plusDays(serviceDays + timeForResponseInDays);

        if (isTooLateForToday(respondedAt.getHour())) {
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
