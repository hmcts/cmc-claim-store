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
    private final int serviceDays;
    private final int timeForResponseInDays;
    private final int endOfBusinessDayHour;

    public DirectionsQuestionnaireDeadlineCalculator(
        WorkingDayIndicator workingDayIndicator,
        @Value("${dateCalculations.serviceDays}") int serviceDays,
        @Value("${dateCalculations.responseDays}") int timeForResponseInDays,
        @Value("${dateCalculations.endOfBusinessDayHour}") int endOfBusinessDayHour) {

        this.workingDayIndicator = workingDayIndicator;
        this.serviceDays = serviceDays;
        this.timeForResponseInDays = timeForResponseInDays;
        this.endOfBusinessDayHour = endOfBusinessDayHour;
    }

    public LocalDate calculateDirectionsQuestionnaireDeadlineCalculator(LocalDateTime respondedAt) {
        return calculateFirstWorkingDayAndAddOffset(respondedAt, serviceDays + timeForResponseInDays);
    }

    private LocalDate calculateFirstWorkingDayAndAddOffset(LocalDateTime dateTime, int offset) {
        LocalDate date = dateTime.toLocalDate();

        if (isTooLateForToday(dateTime.getHour())) {
            date = date.plusDays(1);
        }

        while (!workingDayIndicator.isWorkingDay(date)) {
            date = date.plusDays(1);
        }

        return date.plusDays(offset);
    }

    private boolean isTooLateForToday(int hour) {
        return hour >= endOfBusinessDayHour;
    }
}
