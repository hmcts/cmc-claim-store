package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Calculates response deadline from submission date time.
 */
@Component
public class ResponseDeadlineCalculator {

    private final WorkingDayIndicator workingDayIndicator;
    private final int serviceDays;
    private final int timeForResponseInDays;
    private final int requestedAdditionalTimeInDays;

    public ResponseDeadlineCalculator(
        WorkingDayIndicator workingDayIndicator,
        @Value("${dateCalculations.serviceDays}") final int serviceDays,
        @Value("${dateCalculations.responseDays}") final int timeForResponseInDays,
        @Value("${dateCalculations.requestedAdditionalTimeInDays}") final int requestedAdditionalTimeInDays) {

        this.workingDayIndicator = workingDayIndicator;
        this.serviceDays = serviceDays;
        this.timeForResponseInDays = timeForResponseInDays;
        this.requestedAdditionalTimeInDays = requestedAdditionalTimeInDays;
    }

    /**
     * Calculates response deadline by date of issue.
     */
    public LocalDate calculateResponseDeadline(LocalDate issueDate) {
        return calculateFirstWorkingDayAfterOffset(issueDate, serviceDays + timeForResponseInDays);
    }

    public LocalDate calculatePostponedResponseDeadline(LocalDate currentDeadline) {
        return calculateFirstWorkingDayAfterOffset(currentDeadline, requestedAdditionalTimeInDays);
    }

    private LocalDate calculateFirstWorkingDayAfterOffset(final LocalDate date, final int offset) {
        LocalDate result = date.plusDays(offset);

        while (!workingDayIndicator.isWorkingDay(result)) {
            result = result.plusDays(1);
        }

        return result;
    }
}
